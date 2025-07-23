package com.zhangfuxing.tools.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/5/6
 * @email zhangfuxing1010@163.com
 */
public interface Asyncable<T> {
	void start();

	void stop();

	void stop(long timeout, TimeUnit unit);

	default CompletableFuture<T> asFuture() {
		throw new UnsupportedOperationException("当前实现不支持转换为Future");
	}

	T get();

	T get(long timeout, TimeUnit unit);
}
