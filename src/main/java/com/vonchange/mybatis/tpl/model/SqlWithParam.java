package com.vonchange.mybatis.tpl.model;

import java.util.Arrays;
import java.util.List;

/**
 * @author 冯昌义
 */
public class SqlWithParam {
    private String sql;
    private Object[] params;
    private List<String> propertyNames;

    public String getSql() {
        return sql;
    }

    public List<String> getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "SqlWithParam{" +
                "sql='" + sql + '\'' +
                ", params=" + Arrays.toString(params) +
                '}';
    }
}
