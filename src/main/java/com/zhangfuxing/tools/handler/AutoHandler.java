package com.zhangfuxing.tools.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/1/6
 * @email zhangfuxing1010@163.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AutoHandler {
	int order() default Integer.MAX_VALUE;
}
