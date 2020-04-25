package com.vonchange.mybatis.tpl.sql;



/**
 *
 * Created by von change on 2018/4/19.
 */
public class SqlCommentUtil {
    public static   boolean getLowerNo(String sql){
        if(sql.contains("@lowerNo")){
            return false;
        }
        return  true;
    }
    public static  boolean getOrmNo(String sql){
        if(sql.contains("@ormNo")){
            return false;
        }
        return  true;
    }
    public  static  class Dialect{
        public static final  String MYSQL="mysql";
        public static final  String ORACLE="oracle";
        public static final  String BASE="base";
    }
    public static    String  getDialect(String sql){
        if(sql.contains("@mysql")){
            return Dialect.MYSQL;
        }
        if(sql.contains("@oracle")){
            return Dialect.ORACLE;
        }
        if(sql.contains("@base")){
            return Dialect.BASE;
        }
        return   Dialect.MYSQL;
    }
}
