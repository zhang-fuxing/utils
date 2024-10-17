package com.zhangfuxing.tools.rpc.anno;

import com.zhangfuxing.tools.rpc.RpcBodyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/17
 * @email zhangfuxing1010@163.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface RpcBody {
    RpcBodyType value() default RpcBodyType.JSON;

    String charset() default "UTF-8";
}
