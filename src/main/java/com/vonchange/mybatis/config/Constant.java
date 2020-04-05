package com.vonchange.mybatis.config;

import jodd.bean.BeanUtil;
import jodd.bean.BeanUtilBean;

public class Constant {
    private Constant(){
        throw new IllegalStateException("Utility class");
    }
    public static final BeanUtil BeanUtil= new BeanUtilBean();
    public static final BeanUtil BeanUtilSilent= new BeanUtilBean().silent(true);
    public static final  String PARAM_NOT_NULL="Parameter name must not be null";
}
