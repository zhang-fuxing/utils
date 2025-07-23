package com.zhangfuxing.tools.converter;

import com.zhangfuxing.tools.lambda.LambdaUtil;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * 对象转换器接口
 *
 * @author 张福兴
 * @version 1.0
 * @date 2025/7/22
 * @email zhangfuxing1010@163.com
 */
public interface Converter<T, R> {

	/**
	 * 对象转换器，传入一个对象，返回需要的目标对象
	 * <p>
	 * 例：
	 * <p>
	 * Converter<Integer, String> converter = ObjectConvert.create(Integer.class, String.class);
	 * <p>
	 * String str = converter.convert(1);
	 *
	 * @param sourceInstance 原始类型实例
	 * @return 目标类型实例
	 */
	R conv(T sourceInstance);

	/**
	 * 添加转换规则
	 *
	 * @param rules 转换规则
	 */
	default void rules(Rule<?, ?>... rules) {
	}

	/**
	 * 设置目标对象指定字段的值。
	 * <p>
	 * 通过序列化函数获取字段名，并通过提供的值供应器获取字段值。
	 *
	 * @param field      序列化函数，用于获取目标对象中要设置的字段名
	 * @param fieldValue 值供应器，提供要设置到字段的值
	 */
	default void map(@NotNull LambdaUtil.SerializableFunction<R, ?> field, Function<T, ?> fieldValue) {
		map(LambdaUtil.getName(field), fieldValue);
	}

	/**
	 * 在进行对象转换时通过此方法添加转换规则，将指定的字段名的值通过提供的函数获取并设置到目标对象中。
	 * <p>
	 * 示例：
	 * <pre>
	 * var conv = RefConv.create(class1.class, class2.class);
	 * conv.map("fieldName", (class1 input) -> value);
	 * conv.conv(input);
	 * </pre>
	 *
	 * @param fieldName  目标对象的字段名，可以通过序列化函数获取 例：class1::fieldGetter
	 * @param fieldValue 值获取函数，用于获取字段值
	 */
	default void map(String fieldName, Function<T, ?> fieldValue) {

	}
}
