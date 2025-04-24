package com.zhangfuxing.tools.feignutil;

import feign.Logger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启Feign调用日志
 *
 * @author 张福兴
 * @version 1.0
 * @date 2025/4/24
 * @email zhangfuxing1010@163.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FeignLog {
	Logger.Level value() default Logger.Level.NONE;
}
