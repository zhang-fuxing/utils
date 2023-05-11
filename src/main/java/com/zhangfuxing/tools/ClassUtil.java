package com.zhangfuxing.tools;

import java.lang.reflect.InvocationTargetException;

/**
 * @author zhangfx
 * @date 2023/4/11
 */
public class ClassUtil {
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClass(T obj) {
		return ((null == obj) ? null : (Class<T>) obj.getClass());
	}
	
	public static String getClassName(Class<?> clazz, boolean isSimple) {
		if (null == clazz) {
			return null;
		}
		return isSimple ? clazz.getSimpleName() : clazz.getName();
	}
	
	public static String getClassName(Object obj, boolean isSimple) {
		if (null == obj) {
			return null;
		}
		final Class<?> clazz = obj.getClass();
		return getClassName(clazz, isSimple);
	}
	
	public static <T> T getInstance(Class<T> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		return clazz.getConstructor().newInstance();
	}
	
	public static Short toShort(Integer source, Short def) {
		if (source == null) {
			return def;
		}
		return source.shortValue();
	}
}
