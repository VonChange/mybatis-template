package com.vonchange.mybatis.config;

import com.vonchange.mybatis.common.util.bean.BeanUtil;

public class Constant {
    private Constant(){
        throw new IllegalStateException("Utility class");
    }
    public static final BeanUtil BeanUtil= new BeanUtil();
    public static final  String PARAM_NOT_NULL="Parameter name must not be null";
}
