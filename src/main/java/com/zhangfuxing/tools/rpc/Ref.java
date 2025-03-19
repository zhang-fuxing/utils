package com.zhangfuxing.tools.rpc;

/**
 * Rpc返回值封装类
 *
 * @author 张福兴
 * @version 1.0
 * @date 2025/3/19
 * @email zhangfuxing1010@163.com
 */
public record Ref<T>(T value) {


	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
