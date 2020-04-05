package com.vonchange.mybatis.tpl.extra;

import com.vonchange.mybatis.config.Constant;
import com.vonchange.mybatis.tpl.clazz.ClazzUtils;
import com.vonchange.mybatis.tpl.sql.SqlCommentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;

/**
 * @author 冯昌义
 */
public class DynamicSql {
    private DynamicSql() {
        throw new IllegalStateException("Utility class");
    }

    private static Logger logger = LoggerFactory.getLogger(DynamicSql.class);

    public static String dynamicSql(String sql, Map<String, Object> param) {
        String dialog = SqlCommentUtil.getDialect(sql);
        if (!sql.contains("{@")) {
            return sql;
        }
        String startSym = "{@";
        String endSym = "}";
        int len = sql.length();
        int startLen = startSym.length();
        int endLen = endSym.length();
        int i = 0;
        StringBuilder newSql = new StringBuilder();
        String model = null;
        while (i < len) {
            int ndx = sql.indexOf(startSym, i);
            if (ndx == -1) {
                newSql.append(i == 0 ? sql : sql.substring(i));
                break;
            }
            newSql.append(sql.substring(i, ndx));
            ndx += startLen;
            //newSql
            int ndx2 = sql.indexOf(endSym, ndx);
            if (ndx2 == -1) {
                throw new IllegalArgumentException("无结尾 } 符号 at: " + (ndx - startLen));
            }
            model = sql.substring(ndx, ndx2);
            newSql.append(new StringBuffer(getModel(model, dialog, param)));
            i = ndx2 + endLen;

        }
        logger.debug("自定义语言\n{}", newSql);
        return newSql.toString();
    }

    private static String getModel(String model, String dialog, Map<String, Object> param) {
        model = model.trim();
        model = model.replaceAll("[\\s]+", " ");
        String[] strs = model.split(" ");
        List<String> resultList = new ArrayList<>();
        for (String str : strs) {
            resultList.add(str.trim());
        }
        if (resultList.size() == 4) {
            AnalyeNamed analyeNamed = analyeNamed(resultList, param);
            return workNamed(analyeNamed, dialog);
        }
        return "";
    }

    private static AnalyeNamed analyeNamed(List<String> resultList, Map<String, Object> param) {
        String four = resultList.get(3);
        AnalyeNamed analyeNamed = new AnalyeNamed();
        String link = resultList.get(0);
        String[] paramStrs = four.split(":");
        List<String> strList = new ArrayList<>(Arrays.asList(paramStrs));
        if (strList.size() == 1) {
            strList.add("");
        } else {
            strList.set(1, "." + strList.get(1));
        }
        String named = strList.get(0).trim();
        Object value = getValue(param, named);
        analyeNamed.setValue(value);
        analyeNamed.setType(getValueType(value));
        analyeNamed.setNamedFull(named);
        analyeNamed.setCondition(resultList.get(2));
        analyeNamed.setItemProperty(strList.get(1));
        analyeNamed.setLink(link);
        analyeNamed.setColumn(resultList.get(1));
        return analyeNamed;
    }

    private static Object getValue(Map<String, Object> param, String named) {
        return  Constant.BeanUtilSilent.getProperty(param,named);
    }

    private static String getValueType(Object value) {
        if (null == value) {
            return "base";
        }
        if (value instanceof String) {
            return "string";
        }
        if (Collection.class.isAssignableFrom(value.getClass())) {
            return "list";
        }
        if (value instanceof Object[]) {
            return "arr";
        }
        if (Map.class.isAssignableFrom(value.getClass())) {
            return "map";
        }
        if (ClazzUtils.isBaseType(value.getClass())) {
            return "base";
        }
        return "base";
    }

    private static String workNamed(AnalyeNamed analyeNamed, String dialog) {
        String named = format("#'{'{0}'}'", analyeNamed.getNamedFull());
        if ("in".equals(analyeNamed.getCondition())) {
            named = in(analyeNamed.getNamedFull(), analyeNamed.getItemProperty());
        }
        if (analyeNamed.getCondition().contains("like")){
            named = like(analyeNamed.getNamedFull(), analyeNamed.getCondition(), dialog);
            analyeNamed.setCondition("like");
        }
        String content = format(" {0} {1} {2} {3} ", analyeNamed.getLink(), analyeNamed.getColumn(), analyeNamed.getCondition(), named);
        String ifStr = format("<if test=\"null!={0}\">", analyeNamed.getNamedFull());
        String type = analyeNamed.getType();
        if ("string".equals(type)) {
            ifStr = format("<if test=\"null!={0} and ''''!={0}\">", analyeNamed.getNamedFull());
        }
        if ("list".equals(type)) {
            ifStr = format("<if test=\"null!={0} and {0}.size>0\">", analyeNamed.getNamedFull());
        }
        if ("arr".equals(type)) {
            ifStr = format("<if test=\"null!={0} and {0}.length>0\">", analyeNamed.getNamedFull());
        }
        return format("{0} {1} </if>", ifStr, content);
    }

    private static String like(String named, String likeType, String dialog) {
        String str = "CONCAT(''%'',#'{'{0}'}',''%'') ";
        if (dialog.equals(SqlCommentUtil.Dialect.ORACLE)) {
            str = "''%''||#'{'{0}'}'||''%''";
        }
        if (dialog.equals(SqlCommentUtil.Dialect.BASE)) {
            str = "''%$'{'{0}'}'%''";
        }
        if ("like".equals(likeType) || "5like5".equals(likeType)) {
            return format(str, named);
        }
        str = " CONCAT(#'{'{0}'}',''%'') ";
        if (dialog.equals(SqlCommentUtil.Dialect.ORACLE)) {
            str = "#'{'{0}'}'||''%''";
        }
        if (dialog.equals(SqlCommentUtil.Dialect.BASE)) {
            str = "''$'{'{0}'}'%''";
        }
        if ("like5".equals(likeType)) {
            return format(str, named);
        }
        str = "CONCAT(''%'',#'{'{0}'}') ";
        if (dialog.equals(SqlCommentUtil.Dialect.ORACLE)) {
            str = "''%''||#'{'{0}'}'";
        }
        if (dialog.equals(SqlCommentUtil.Dialect.BASE)) {
            str = "''%$'{'{0}'}'";
        }
        if ("5like".equals(likeType)) {
            return format(str, named);
        }
        return format("#'{'{0}'}'", named);
    }

    public static String in(String named, String itemProperty) {
        String str = "<foreach collection=\"{0}\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">" +
                "#'{'item{1}'}'" +
                "</foreach>";
        return format(str, named, itemProperty);
    }

    public static String format(String pattern, Object... arguments) {
        MessageFormat temp = new MessageFormat(pattern);
        return temp.format(arguments);
    }
}
