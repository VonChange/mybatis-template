package com.vonchange.mybatis.tpl;


import com.vonchange.mybatis.tpl.exception.MybatisMinRuntimeException;
import com.vonchange.mybatis.tpl.extra.DynamicSql;
import com.vonchange.mybatis.tpl.model.SqlWithParam;
import com.vonchange.mybatis.common.util.StringUtils;
import jodd.util.StringUtil;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * mybatis 模版主要代码
 */
public class MybatisTpl {
    private static   Logger logger = LoggerFactory.getLogger(MybatisTpl.class);
    private MybatisTpl() {
        throw new IllegalStateException("Utility class");
    }
     @SuppressWarnings("unchecked")
     public static SqlWithParam generate(String sqlInXml, Map<String,Object> parameter){
         SqlWithParam sqlWithParam= new SqlWithParam();
        if(StringUtil.isBlank(sqlInXml)){
            sqlWithParam.setSql(null);
            sqlWithParam.setParams(null);
            return  sqlWithParam;
        }
         sqlInXml= DynamicSql.dynamicSql(sqlInXml,parameter);
         sqlInXml=sqlInXml.trim();
         if(sqlInXml.contains("</")){
             sqlInXml="<script>"+sqlInXml+"</script>";
             sqlInXml =  StringUtils.replaceEach(sqlInXml,new String[]{" > "," < "," >= "," <= "," <> "},
                     new String[]{" &gt; "," &lt; "," &gt;= "," &lt;= "," &lt;&gt; "});
         }
         if(null==parameter){
             parameter=new LinkedHashMap<>();
         }
         LanguageDriver languageDriver = new XMLLanguageDriver();
         Configuration configuration= new Configuration();
         Properties properties= new Properties();
         for (Map.Entry<String,Object> entry: parameter.entrySet()) {
             if(null==entry.getValue()){
                 continue;
             }
             properties.put(entry.getKey(),entry.getValue());
         }
         configuration.setVariables(properties);
         BoundSql boundSql = null;
         try {
             SqlSource sqlSource = languageDriver.createSqlSource(configuration, sqlInXml, Map.class);
              boundSql=sqlSource.getBoundSql(parameter);
         }catch (Exception e){
             logger.error("解析sqlxml出错",e);
             logger.error("sqlxml:{}",sqlInXml);
             sqlWithParam.setSql(null);
             sqlWithParam.setParams(null);
             return  sqlWithParam;
         }

         if(boundSql.getSql().contains("#{")){
             return generate(boundSql.getSql(),parameter);
         }
         List<ParameterMapping> list= boundSql.getParameterMappings();
         List<Object> argList= new ArrayList<>();
         List<String> propertyNames = new ArrayList<>();
         if(null!=list&&!list.isEmpty()){
             Map<String,Object> param =new LinkedHashMap<>();
             if(boundSql.getParameterObject() instanceof  Map){
                       param = (Map<String, Object>) boundSql.getParameterObject();
             }
             for (ParameterMapping parameterMapping: list) {
                 Object value;

                 String propertyName= parameterMapping.getProperty();
                 if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
                     value = boundSql.getAdditionalParameter(propertyName);
                 } else if (param == null) {
                     value = null;
                 }else {
                     MetaObject metaObject = configuration.newMetaObject(param);
                     if(!metaObject.hasGetter(propertyName)){
                         throw  new MybatisMinRuntimeException(propertyName+"占位符值不存在!");
                     }
                     value = metaObject.getValue(propertyName);
                 }
                 argList.add(value);
                 propertyNames.add(propertyName);
             }
         }
         Object[] args=argList.toArray(new Object[argList.size()]);
         String sql=boundSql.getSql();
         sqlWithParam.setSql(sql);
         sqlWithParam.setParams(args);
         sqlWithParam.setPropertyNames(propertyNames);
         return sqlWithParam;
     }


}
