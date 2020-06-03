package com.vonchange.mybatis.tpl.extra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 冯昌义
 */
public class DynamicSql {
    private DynamicSql() {
        throw new IllegalStateException("Utility class");
    }

    private static Logger logger = LoggerFactory.getLogger(DynamicSql.class);
    private static final String PALCEHOLDERA="#'{'{0}'}'";

    public static String dynamicSql(String sql) {
        if (sql.contains("[@")) {
            return dynamicSqlDo(sql);
        }
        //未来版本不再支持
        if (sql.contains("{@")) {
            return dynamicSqlOld(sql);
        }
        return sql;
    }
    private static String dynamicSqlDo(String sql){
        String startSym = "[@";
        String endSym = "]";
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
                throw new IllegalArgumentException("无结尾 ] 符号 at: " + (ndx - startLen));
            }
            model = sql.substring(ndx, ndx2);
            newSql.append(new StringBuffer(getModel(model)));
            i = ndx2 + endLen;
        }
        logger.debug("自定义语言\n{}", newSql);
        return newSql.toString();
    }
    private static String dynamicSqlOld(String sql){
        String startSym = "{@";
        String endSym = "}";
        int len = sql.length();
        int startLen = startSym.length();
        int endLen = endSym.length();
        int i = 0;
        StringBuilder newSql = new StringBuilder();
        String model;
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
            newSql.append(new StringBuffer(getModel(model)));
            i = ndx2 + endLen;
        }
        logger.debug("自定义语言\n{}", newSql);
        return newSql.toString();
    }

    private static String getModel(String model) {
        if(model.contains("#{")){
            return ifNull(model);
        }
        model = model.trim();
        model = model.replaceAll("[\\s]+", " ");
        String[] moduleSubs = model.split(" ");
        List<String> resultList = new ArrayList<>();
        for (String str : moduleSubs) {
            resultList.add(str.trim());
        }
        if (resultList.size() == 4) {
            AnalyeNamed analyeNamed = analyeNamed(resultList);
            return workNamed(analyeNamed);
        }
        //扩展只有判空
        return "";
    }
    private static String ifNull(String model){
        SqlParamResult sqlParamResult = getParamFromModel(model);
        StringBuilder sb= new StringBuilder();
        for (String param:sqlParamResult.getParam()) {
            sb.append(format("@com.vonchange.mybatis.tpl.MyOgnl@isNotEmpty({0}) and ",param));
        }
        String ifStr = format("<if test=\"{0}\">",sb.substring(0,sb.length()-4));
        return format("{0} {1} </if>", ifStr, sqlParamResult.getNewSql());

    }
    private static SqlParamResult getParamFromModel(String model){
        String startSym = "#{";
        String endSym = "}";
        int len = model.length();
        int startLen = startSym.length();
        int endLen = endSym.length();
        int i = 0;
        Map<String,Boolean> paramMap = new HashMap<>();
        StringBuilder newSql = new StringBuilder();
        String param;
        while (i < len) {
            int ndx = model.indexOf(startSym, i);
            if (ndx == -1) {
                newSql.append(i == 0 ? model : model.substring(i));
                break;
            }
            newSql.append(model.substring(i, ndx));
            ndx += startLen;
            int ndx2 = model.indexOf(endSym, ndx);
            if (ndx2 == -1) {
                throw new IllegalArgumentException("无结尾 } 符号 at: " + (ndx - startLen));
            }
            param=model.substring(ndx, ndx2);
            paramMap.put(getTrueParam(param),true);
            newSql.append(getParamSql(param));
            i = ndx2 + endLen;
        }
        List<String> list = new ArrayList<>();
        for(Map.Entry<String,Boolean> entry:paramMap.entrySet()){
            list.add(entry.getKey());
        }
        return new SqlParamResult(list,newSql.toString());
    }
    private static String getParamSql(String param){
        if(!param.contains(":")){
            return format(PALCEHOLDERA, param);
        }
        String[] params =  param.split(":");
        if("in".equalsIgnoreCase(params[params.length-1])){
            String itemProperty="";
            if(params.length>2){
                itemProperty="."+params[2];
            }
            return in(params[0],itemProperty);
        }
        if("like".equalsIgnoreCase(params[params.length-1])){
            AnalyeNamed analyeNamed = new AnalyeNamed();
            analyeNamed.setNamedFull(params[0]);
            return like(analyeNamed);
        }
        return format(PALCEHOLDERA, param);
    }
    private static String getTrueParam(String param){
        if(!param.contains(":")){
            return param;
        }
        String[] params =  param.split(":");
        if("like".equalsIgnoreCase(params[params.length-1])){
            return params[0].replace("%","");
        }
        return params[0];
    }

    private static AnalyeNamed analyeNamed(List<String> resultList) {
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
        analyeNamed.setNamedFull(named);
        analyeNamed.setCondition(resultList.get(2));
        analyeNamed.setItemProperty(strList.get(1));
        analyeNamed.setLink(link);
        analyeNamed.setColumn(resultList.get(1));
        return analyeNamed;
    }


    private static String workNamed(AnalyeNamed analyeNamed) {
        String named = format(PALCEHOLDERA, analyeNamed.getNamedFull());
        if ("in".equalsIgnoreCase(analyeNamed.getCondition())) {
            named = in(analyeNamed.getNamedFull(), analyeNamed.getItemProperty());
        }
        if ("like".equalsIgnoreCase(analyeNamed.getCondition())){
            named = like(analyeNamed);
        }
        String content = format(" {0} {1} {2} {3} ", analyeNamed.getLink(), analyeNamed.getColumn(), analyeNamed.getCondition(), named);
        String ifStr = format("<if test=\"@com.vonchange.mybatis.tpl.MyOgnl@isNotEmpty({0})\">", analyeNamed.getNamedFull());
        return format("{0} {1} </if>", ifStr, content);
    }

    private static String likeOld(AnalyeNamed analyeNamed) {
        String named=analyeNamed.getNamedFull();
        boolean all=!named.contains("%");
        boolean left = named.startsWith("%");
        boolean right =named.endsWith("%");
        String str = "CONCAT(''%'',#'{'{0}'}',''%'') ";
        if (all) {
            return format(str, named);
        }
        if(left&&right){
            analyeNamed.setNamedFull(named.substring(1,named.length()-1));
            return format(str, analyeNamed.getNamedFull());
        }
        str = " CONCAT(#'{'{0}'}',''%'') ";
        if (right) {
            analyeNamed.setNamedFull(named.substring(0,named.length()-1));
            return format(str, analyeNamed.getNamedFull());
        }
        str = "CONCAT(''%'',#'{'{0}'}') ";
        if (left) {
            analyeNamed.setNamedFull(named.substring(1));
            return format(str, analyeNamed.getNamedFull());
        }
        return format(PALCEHOLDERA, named);
    }

    /**
     * bind 方式 能通用
     */
    private static String like(AnalyeNamed analyeNamed) {
        String named=analyeNamed.getNamedFull();
        boolean all=!named.contains("%");
        boolean left = named.startsWith("%");
        boolean right =named.endsWith("%");
        // <bind name="usrName" value="'%' + name + '%'"/>
        String str = "<bind name=\"{0}\" value=\"''%'' + {1} + ''%''\"/>  #'{'{2}'}";
        if (all) {
            return likeFormat(str, named,"_like");
        }
        if(left&&right){
            analyeNamed.setNamedFull(named.substring(1,named.length()-1));
            return likeFormat(str, analyeNamed.getNamedFull(),"_like");
        }
        str = "<bind name=\"{0}\" value=\"{1} + ''%''\"/>  #'{'{2}'}";
        if (right) {
            analyeNamed.setNamedFull(named.substring(0,named.length()-1));
            return likeFormat(str, analyeNamed.getNamedFull(),"_rightLike");
        }
        str = "<bind name=\"{0}\" value=\"''%'' +{1} \"/>  #'{'{2}'}";
        if (left) {
            analyeNamed.setNamedFull(named.substring(1));
            return likeFormat(str, analyeNamed.getNamedFull(),"_leftLike");
        }
        return format(PALCEHOLDERA, named);
    }
    private static String likeFormat(String tpl,String named,String type){
        return format(tpl, named+type,named,named+type);
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
