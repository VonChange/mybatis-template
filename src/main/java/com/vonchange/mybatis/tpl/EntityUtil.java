package com.vonchange.mybatis.tpl;

import com.vonchange.mybatis.common.util.StringUtils;
import com.vonchange.mybatis.tpl.annotation.*;
import com.vonchange.mybatis.tpl.clazz.ClazzUtils;
import com.vonchange.mybatis.tpl.model.EntityField;
import com.vonchange.mybatis.tpl.model.EntityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Created by 冯昌义 on 2018/4/19.
 */
public class EntityUtil {
    private static final Map<String, EntityInfo> entityMap = new ConcurrentHashMap<>();
    private static Logger logger = LoggerFactory.getLogger(EntityUtil.class);
    public static void initEntityInfo(Class<?> clazz) {
        String entityName = clazz.getName();
        if(!entityMap.containsKey(entityName)){
            initEntity(clazz);
        }
    }

    public static  EntityInfo getEntityInfo(Class<?> clazz){
        return entityMap.get(clazz.getName());
    }

    private static  void initEntity(Class<?> clazz) {
        logger.debug("初始化 {}", clazz.getName());
        EntityInfo entity = new EntityInfo();
        Table table=clazz.getAnnotation(Table.class);
        String tableName=null;
        if(null!=table){
            tableName=table.name();
        }
        if(StringUtils.isBlank(tableName)){
            String tableEntity= clazz.getSimpleName();
            if(clazz.getSimpleName().toLowerCase().endsWith("do")){
                tableEntity=clazz.getSimpleName().substring(0,clazz.getSimpleName().length()-2);
            }
            tableName= OrmUtil.toSql(tableEntity);
        }
        entity.setTableName(tableName);
        List<Field> fieldList = new ArrayList<>();
        getFieldList(clazz,fieldList);
                //clazz.getDeclaredFields();// 只有本类
        Map<String, EntityField> entityFieldMap = new LinkedHashMap<>();
        Column column;
        List<String> columnReturns = new ArrayList<>();
        for (Field field : fieldList) {
            Class<?> type = field.getType();
            Boolean isBaseType = ClazzUtils.isBaseType(type);
            String fieldName = field.getName();
            if(Boolean.FALSE.equals(isBaseType)||entityFieldMap.containsKey(fieldName)){
                continue;
            }
            EntityField entityField = new EntityField();
            entityField.setFieldName(fieldName);
            column=field.getAnnotation(Column.class);
            String columnName =null;
            if(null!=column){
                columnName=column.name();
            }
            if(StringUtils.isBlank(columnName)){
                columnName = OrmUtil.toSql(fieldName);
            }
            entityField.setColumnName(columnName);
            entityField.setTypeName(type.getSimpleName());
            entityField.setIsBaseType(isBaseType);
            entityField.setIsColumn(true);
            entityField.setUpdateNotNull(false);
            entityField.setIgnoreDupUpdate(false);
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof ColumnNot) {
                    entityField.setIsColumn(false);
                    continue;
                }
                if (annotation instanceof Id) {
                    entityField.setIsId(true);
                    entity.setIdFieldName(fieldName);
                    entity.setIdColumnName(columnName);
                    entity.setIdType(type.getSimpleName());
                    if(null==entity.getGenColumn()){
                        entity.setGenColumn(columnName);
                    }
                   // continue;
                }
                if(annotation instanceof GeneratedValue){
                    entity.setGenColumn(columnName);
                }
                if(annotation instanceof InsertReturn){
                    columnReturns.add(columnName);
                }
                if (annotation instanceof UpdateNotNull) {
                    entityField.setUpdateNotNull(true);
                }
                if (annotation instanceof InsertIfNull) {
                    entityField.setInsertIfNull(((InsertIfNull) annotation).value());
                    entityField.setInsertIfNullFunc(((InsertIfNull) annotation).function());
                }
                if (annotation instanceof UpdateIfNull) {
                    entityField.setUpdateIfNull(((UpdateIfNull) annotation).value());
                    entityField.setUpdateIfNullFunc(((UpdateIfNull) annotation).function());
                }
                if (annotation instanceof UpdateDuplicateKeyIgnore) {
                    entityField.setIgnoreDupUpdate(true);
                }
            }
            entityFieldMap.put(fieldName, entityField);
        }
        entity.setColumnReturns(columnReturns);
        entity.setFieldMap(entityFieldMap);
        entityMap.put(clazz.getName(), entity);
    }

    private static void getFieldList(Class<?> clazz, List<Field> fieldList) {
        fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
        if(null!=clazz.getSuperclass()){
            getFieldList(clazz.getSuperclass(), fieldList);
        }

    }
}
