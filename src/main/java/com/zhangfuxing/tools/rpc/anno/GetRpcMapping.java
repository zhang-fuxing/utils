package com.zhangfuxing.tools.rpc.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.zhangfuxing.tools.rpc.Method.GET;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/3/28
 * @email zhangfuxing1010@163.com
 */
@RpcMapping(method = GET)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface GetRpcMapping {
	String value() default "";

	String[] headers() default {"Content-Type: application/json"};

	long timeout() default -1;
}
