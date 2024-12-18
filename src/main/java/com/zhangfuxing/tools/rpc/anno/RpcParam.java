package com.zhangfuxing.tools.rpc.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/15
 * @email zhangfuxing1010@163.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface RpcParam {
    String value() default "";

    boolean required() default true;
}
