package com.vonchange.mybatis.tpl.extra;

import com.vonchange.mybatis.dialect.Dialect;
import com.vonchange.mybatis.dialect.LikeTemplate;
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

    public static String dynamicSql(String sql, Dialect dialect) {
        if (sql.contains("[@")) {
            return dynamicSqlDo(sql,dialect);
        }
        //未来版本不再支持
        if (sql.contains("{@")) {
            return dynamicSqlOld(sql,dialect);
        }
        return sql;
    }
    private static String dynamicSqlDo(String sql,Dialect dialect){
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
            newSql.append(new StringBuffer(getModel(model,dialect)));
            i = ndx2 + endLen;
        }
        logger.debug("自定义语言\n{}", newSql);
        return newSql.toString();
    }
    private static String dynamicSqlOld(String sql,Dialect dialect){
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
            newSql.append(new StringBuffer(getModel(model,dialect)));
            i = ndx2 + endLen;
        }
        logger.debug("自定义语言\n{}", newSql);
        return newSql.toString();
    }

    private static String getModel(String model,Dialect dialect) {
        if(model.contains("#{")){
            return ifNull(model,dialect);
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
            return workNamed(analyeNamed,dialect);
        }
        //扩展只有判空
        return "";
    }
    private static String ifNull(String model,Dialect dialect){
        SqlParamResult sqlParamResult = getParamFromModel(model,dialect);
        if(model.startsWith("@")){
               return sqlParamResult.getNewSql().substring(1);
        }
        StringBuilder sb= new StringBuilder();
        for (String param:sqlParamResult.getParam()) {
            sb.append(format("@com.vonchange.mybatis.tpl.MyOgnl@isNotEmpty({0}) and ",param));
        }
        String ifStr = format("<if test=\"{0}\">",sb.substring(0,sb.length()-4));
        return format("{0} {1} </if>", ifStr, sqlParamResult.getNewSql());

    }
    private static SqlParamResult getParamFromModel(String model,Dialect dialect){
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
            newSql.append(getParamSql(param,dialect));
            i = ndx2 + endLen;
        }
        List<String> list = new ArrayList<>();
        for(Map.Entry<String,Boolean> entry:paramMap.entrySet()){
            list.add(entry.getKey());
        }
        return new SqlParamResult(list,newSql.toString());
    }
    private static String getParamSql(String param,Dialect dialect){
        if(!param.contains(":")){
            if(param.contains("%")){
                AnalyeNamed analyeNamed = new AnalyeNamed();
                analyeNamed.setNamedFull(param);
                return like(analyeNamed,dialect);
            }
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
            return like(analyeNamed,dialect);
        }
        return format(PALCEHOLDERA, param);
    }
    private static String getTrueParam(String param){
        if(!param.contains(":")){
            if(param.contains("%")){
                return param.replace("%","");
            }
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


    private static String workNamed(AnalyeNamed analyeNamed,Dialect dialect) {
        String named = format(PALCEHOLDERA, analyeNamed.getNamedFull());
        if ("in".equalsIgnoreCase(analyeNamed.getCondition())) {
            named = in(analyeNamed.getNamedFull(), analyeNamed.getItemProperty());
        }
        if ("like".equalsIgnoreCase(analyeNamed.getCondition())){
            named = like(analyeNamed,dialect);
        }
        boolean ifNullFlag=true;
        String link = analyeNamed.getLink();
        if(analyeNamed.getLink().startsWith("@")){
            ifNullFlag=false;
            link=link.substring(1);
        }
        String content = format(" {0} {1} {2} {3} ", link, analyeNamed.getColumn(), analyeNamed.getCondition(), named);
        if(!ifNullFlag){
            return content.substring(1);
        }
        String ifStr = format("<if test=\"@com.vonchange.mybatis.tpl.MyOgnl@isNotEmpty({0})\">", analyeNamed.getNamedFull());
        return format("{0} {1} </if>", ifStr, content);
    }

    private static String like(AnalyeNamed analyeNamed,Dialect dialect) {
        String named=analyeNamed.getNamedFull();
        boolean all=!named.contains("%");
        boolean left = named.startsWith("%");
        boolean right =named.endsWith("%");
        LikeTemplate likeTemplate = dialect.getLikeTemplate();
        String str = likeTemplate.getFull();
        if (all) {
            return format(str, named);
        }
        if(left&&right){
            analyeNamed.setNamedFull(named.substring(1,named.length()-1));
            return format(str, analyeNamed.getNamedFull());
        }
        str = likeTemplate.getRight();
        if (right) {
            analyeNamed.setNamedFull(named.substring(0,named.length()-1));
            return format(str, analyeNamed.getNamedFull());
        }
        str = likeTemplate.getLeft();
        if (left) {
            analyeNamed.setNamedFull(named.substring(1));
            return format(str, analyeNamed.getNamedFull());
        }
        return format(PALCEHOLDERA, named);
    }

    /**
     * bind 方式 能通用 有bug = 和 like一起用
     */
    private static String likeNew(AnalyeNamed analyeNamed) {
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
