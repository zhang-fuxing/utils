package com.zhangfuxing.tools;

import java.util.function.Function;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2023/05/11
 * @email zhangfuxing1010@163.com
 */
public class ConvertUtil {
	
	public static <T, R> R convert(T t, Function<T, R> action) {
		return action.apply(t);
	}
	// short
	public static Short toShort(Integer source) {
		return convert(source, t -> t == null ? null : t.shortValue());
	}
	public static Short toShort(Long source) {
		return convert(source, t -> t == null ? null : t.shortValue());
	}
	public static Short toShort(String source) {
		Short convert = null;
		try {
			convert = convert(source, t -> StringUtil.isBlank(source) ? null : Short.parseShort(t));
		} catch (Exception ignored) {}
		return convert;
	}
	public static Short toShort(Float source) {
		return convert(source, t -> t == null ? null : t.shortValue());
	}
	public static Short toShort(Double source) {
		return convert(source, t -> t == null ? null : t.shortValue());
	}
	
	// int
	public static Integer toInt(Double source) {
		return convert(source, t -> t == null ? null : t.intValue());
	}
	public static Integer toInt(Float source) {
		return convert(source, t -> t == null ? null : t.intValue());
	}
	public static Integer toInt(Long source) {
		return convert(source, t -> t == null ? null : t.intValue());
	}
	public static Integer toInt(Short source) {
		return convert(source, t -> t == null ? null : t.intValue());
	}
	public static Integer toInt(String source) {
		Integer convert = null;
		try {
			convert = convert(source, t -> StringUtil.isBlank(source) ? null : Integer.parseInt(t));
		} catch (Exception ignored) {}
		return convert;
	}
	
	// long
	public static Long toLong(Double source) {
		return convert(source, t -> t == null ? null : t.longValue());
	}
	public static Long toLong(Float source) {
		return convert(source, t -> t == null ? null : t.longValue());
	}
	public static Long toLong(Integer source) {
		return convert(source, t -> t == null ? null : t.longValue());
	}
	public static Long toLong(Short source) {
		return convert(source, t -> t == null ? null : t.longValue());
	}
	public static Long toLong(String source) {
		Long convert = null;
		try {
			convert = convert(source, t -> StringUtil.isBlank(source) ? null : Long.parseLong(t));
		} catch (Exception ignored) {}
		return convert;
	}
}
