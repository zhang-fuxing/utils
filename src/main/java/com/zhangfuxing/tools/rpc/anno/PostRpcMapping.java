package com.zhangfuxing.tools.rpc.anno;

import com.zhangfuxing.tools.rpc.Method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/3/28
 * @email zhangfuxing1010@163.com
 */
@RpcMapping(method = Method.POST)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface PostRpcMapping {
	String value() default "";

	String[] headers() default {"Content-Type: application/json"};

	long timeout() default -1;
}
