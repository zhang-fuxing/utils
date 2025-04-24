package com.zhangfuxing.tools.feignutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 设置 Feign 的目标地址
 *
 * @author 张福兴
 * @version 1.0
 * @date 2025/4/24
 * @email zhangfuxing1010@163.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FeignTarget {
	String value();
}
