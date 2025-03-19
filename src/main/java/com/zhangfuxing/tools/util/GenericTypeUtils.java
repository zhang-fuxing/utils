package com.zhangfuxing.tools.util;

import java.lang.reflect.*;

/**
 * 泛型类型工具类
 * 功能：
 * 1. 获取方法返回值的泛型类型
 * 2. 获取方法参数的泛型类型
 * 3. 获取类/接口的泛型类型
 * 4. 获取字段的泛型类型
 *
 * @author 张福兴
 * @version 1.0
 * @date 2025/3/19
 * @email zhangfuxing1010@163.com
 */
public class GenericTypeUtils {

	/**
	 * 获取方法返回值的泛型类型
	 *
	 * @param method 目标方法
	 * @param index  泛型参数索引（从0开始）
	 * @return 实际的泛型类型
	 */
	public static Type getReturnGenericType(Method method, int index) {
		Type returnType = method.getGenericReturnType();
		return resolveGenericType(returnType, index);
	}

	/**
	 * 获取方法参数的泛型类型
	 *
	 * @param method       目标方法
	 * @param paramIndex   参数索引（从0开始）
	 * @param genericIndex 泛型参数索引（从0开始）
	 * @return 实际的泛型类型
	 */
	public static Type getParameterGenericType(Method method, int paramIndex, int genericIndex) {
		Type[] paramTypes = method.getGenericParameterTypes();
		if (paramIndex >= paramTypes.length) {
			throw new IllegalArgumentException("参数索引超出范围");
		}
		return resolveGenericType(paramTypes[paramIndex], genericIndex);
	}

	/**
	 * 获取类/接口的泛型类型（适用于继承/实现场景）
	 *
	 * @param clazz      目标类
	 * @param superClass 父类或接口的Class
	 * @param index      泛型参数索引
	 * @return 实际的泛型类型
	 */
	public static Type getClassGenericType(Class<?> clazz, Class<?> superClass, int index) {
		Type type = findSuperType(clazz, superClass);
		return resolveGenericType(type, index);
	}

	/**
	 * 获取字段的泛型类型
	 *
	 * @param field 目标字段
	 * @param index 泛型参数索引
	 * @return 实际的泛型类型
	 */
	public static Type getFieldGenericType(Field field, int index) {
		Type genericType = field.getGenericType();
		return resolveGenericType(genericType, index);
	}

	// 解析泛型类型
	private static Type resolveGenericType(Type type, int index) {
		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			Type[] actualTypes = pt.getActualTypeArguments();
			if (index >= actualTypes.length) {
				throw new IllegalArgumentException("泛型索引超出范围");
			}
			Type actualType = actualTypes[index];
			// 处理嵌套泛型
			if (actualType instanceof ParameterizedType || actualType instanceof GenericArrayType) {
				return actualType;
			}
			// 处理通配符类型
			if (actualType instanceof WildcardType) {
				WildcardType wt = (WildcardType) actualType;
				Type[] upperBounds = wt.getUpperBounds();
				return upperBounds.length > 0 ? upperBounds[0] : Object.class;
			}
			return actualType;
		}
		throw new IllegalArgumentException("类型不包含泛型参数");
	}

	// 查找父类/接口的类型信息
	private static Type findSuperType(Class<?> clazz, Class<?> superClass) {
		// 检查父类
		Type superType = clazz.getGenericSuperclass();
		if (superType != null && getRawType(superType) == superClass) {
			return superType;
		}

		// 检查接口
		for (Type interfaceType : clazz.getGenericInterfaces()) {
			if (getRawType(interfaceType) == superClass) {
				return interfaceType;
			}
		}

		// 递归查找父类
		Class<?> superClazz = clazz.getSuperclass();
		if (superClazz != null && superClazz != Object.class) {
			Type result = findSuperType(superClazz, superClass);
			if (result != null) return result;
		}

		throw new IllegalArgumentException("未找到指定的父类或接口");
	}

	// 获取原始类型
	private static Class<?> getRawType(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		}
		return null;
	}

	/**
	 * 获取嵌套泛型的实际类型（支持多级解析）
	 * 示例：List<Map<String, Integer>> 解析为 String.class, Integer.class
	 *
	 * @param type    原始类型
	 * @param indexes 多级索引数组
	 */
	public static Type getNestedGenericType(Type type, int... indexes) {
		Type current = type;
		for (int index : indexes) {
			if (current instanceof ParameterizedType pt) {
				Type[] actualTypes = pt.getActualTypeArguments();
				if (index >= actualTypes.length) {
					throw new IllegalArgumentException("泛型索引超出范围");
				}
				current = actualTypes[index];
			} else if (current instanceof GenericArrayType gat) {
				current = gat.getGenericComponentType();
			} else {
				throw new IllegalArgumentException("当前类型不包含泛型参数");
			}
		}
		return current;
	}
}
