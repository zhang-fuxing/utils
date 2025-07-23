package com.zhangfuxing.tools.converter;

import java.util.function.Function;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/7/22
 * @email zhangfuxing1010@163.com
 */
public record Rule<T, R>(Class<T> clazz, Class<R> targetClass, Function<T, R> rule) {

	public static <T, R> Rule<T, R> of(Class<T> clazz, Function<T, R> rule) {
		return new Rule<>(clazz, null, rule);
	}

	public static <T, R> Rule<T, R> of(Class<T> clazz, Class<R> targetClass, Function<T, R> rule) {
		return new Rule<>(clazz, targetClass, rule);
	}


	public Rule(Class<T> clazz, Function<T, R> rule) {
		this(clazz, null, rule);
	}
}
