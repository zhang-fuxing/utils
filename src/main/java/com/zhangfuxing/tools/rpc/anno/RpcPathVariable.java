package com.zhangfuxing.tools.rpc.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RESTful 风格的路径参数注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RpcPathVariable {
    /**
     * 参数名称
     */
    String value();

    /**
     * 是否必须，默认为true
     */
    boolean required() default true;
} 