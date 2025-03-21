package com.zhangfuxing.tools.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel列和Bean对象的关系映射注解，用于在读取Excel文件时，将Excel列与Bean对象的属性进行映射。
 * <p>
 * 适用与类字段的注解，value 属性表示Excel列名，如果为空则默认为字段名
 *
 * @author 张福兴
 * @version 1.0
 * @date 2025/2/27
 * @email zhangfuxing1010@163.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ExcelHeader {

	/**
	 * Excel列名
	 *
	 * @return 默认为字段名
	 */
	String value() default "";

	/**
	 * 导出到excel时表头的顺序
	 *
	 * @return 默认为100，越小越靠前
	 */
	int index() default 100;

	/**
	 * 导出到excel时表头的示例
	 *
	 * @return 默认为空
	 */
	String example() default "";
}
