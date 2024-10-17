package com.zhangfuxing.tools.rpc.anno;

import com.zhangfuxing.tools.rpc.Method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.zhangfuxing.tools.rpc.Method.GET;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/15
 * @email zhangfuxing1010@163.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RpcMapping {
    String value() default "";

    Method method() default GET;

    String[] headers() default {"Content-Type: application/json"};

    Class<?> responseType() default String.class;
}
