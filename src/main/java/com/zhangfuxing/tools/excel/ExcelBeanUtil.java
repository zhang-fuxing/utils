package com.zhangfuxing.tools.excel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/2/27
 * @email zhangfuxing1010@163.com
 */
public class ExcelBeanUtil {
	private static final Logger log = LoggerFactory.getLogger(ExcelBeanUtil.class);

	public static <T> List<T> readBean(String filepath, Supplier<T> beanSupplier, int rid) {
		Map<String, BiConsumer<T, Object>> map = createMapping(beanSupplier);
		return ExcelLoader.createBeanLoader(beanSupplier)
				.addColumnMapping(map)
				.load(filepath, rid);
	}

	public static <T> List<T> readBean(File file, Supplier<T> beanSupplier, int rid) {
		return ExcelLoader.createBeanLoader(beanSupplier)
				.addColumnMapping(createMapping(beanSupplier))
				.load(file, rid);
	}

	public static <T> List<T> readBean(String filepath, Supplier<T> beanSupplier) {
		return readBean(filepath, beanSupplier, 0);
	}

	public static <T> List<T> readBean(File file, Supplier<T> beanSupplier) {
		return readBean(file, beanSupplier, 0);
	}

	public static <T> List<T> readBean(InputStream inputStream, Supplier<T> beanSupplier, int rid) {
		return ExcelLoader.createBeanLoader(beanSupplier)
				.addColumnMapping(createMapping(beanSupplier))
				.load(inputStream, rid);
	}

	public static <T> List<T> readBean(InputStream inputStream, Supplier<T> beanSupplier) {
		return readBean(inputStream, beanSupplier, 0);
	}

	/**
	 * 根据Java类创建Excel模板，并写入输出流中，默认关闭输出流
	 *
	 * @param outputStream 输出流
	 * @param beanSupplier BeanSupplier
	 */
	public static <T> void writeTemplate(OutputStream outputStream, Supplier<T> beanSupplier) {
		writeTemplate(outputStream, beanSupplier, true);
	}

	/**
	 * 根据Java类创建Excel模板，并写入输出流中
	 *
	 * @param outputStream 输出流
	 * @param beanSupplier BeanSupplier
	 * @param autoClose    是否自动关闭输出流
	 */
	public static <T> void writeTemplate(OutputStream outputStream, Supplier<T> beanSupplier, boolean autoClose) {
		Map<String, BiConsumer<T, Object>> mapping = createMapping(beanSupplier);
		ExcelLoader.writeTemplate(outputStream, mapping, autoClose);
	}

	private static <T> Map<String, BiConsumer<T, Object>> createMapping(Supplier<T> beanSupplier) {
		T instance = beanSupplier.get();
		Map<String, BiConsumer<T, Object>> map = new LinkedHashMap<>();
		var fieldSet = Arrays.stream(ReflectUtil.getFields(instance.getClass()))
				.filter(field -> field.isAnnotationPresent(ExcelHeader.class))
				.toList();
		for (Field field : fieldSet) {
			ExcelHeader excelHeader = field.getAnnotation(ExcelHeader.class);
			// 获取字段对应的Setter方法
			String name = field.getName();
			Method setMethod = ReflectUtil.getMethodByName(instance.getClass(), "set" + StrUtil.upperFirst(name));
			if (setMethod == null) {
				// 为了兼容不规范的字段名，尝试获取小写的Setter方法
				setMethod = ReflectUtil.getMethodByName(instance.getClass(), "set" + name);
			}
			if (setMethod == null) {
				// 如果找不到对应的Setter方法，尝试获取以"set"开头的任意方法
				Method[] methods = ReflectUtil.getMethods(instance.getClass(), method -> method.getName().startsWith("set"));
				for (Method method : methods) {
					String lowerCase = method.getName().toLowerCase();
					// 全部转换为小写查找，如果包含目标字段名则认为找到了对应的Setter方法
					if (lowerCase.contains(name.toLowerCase())) {
						setMethod = method;
						break;
					}
				}
			}

			Method finalSetMethod = setMethod;
			map.put(excelHeader.value(), (o, p) -> {
				Object inputValue = Convert.convert(field.getType(), p);
				if (finalSetMethod != null) {
					try {
						ReflectUtil.invoke(o, finalSetMethod, inputValue);
					} catch (Exception e) {
						// 如果使用方法设置字段失败，则使用反射设置字段值
						ReflectUtil.setFieldValue(o, field, inputValue);
					}
				} else {
					ReflectUtil.setFieldValue(o, field, inputValue);
				}
			});
		}
		return map;
	}

	/**
	 * 将数据写入到Excel文件中，并写入输出流中
	 *
	 * @param rowList      数据列表
	 * @param outputStream 输出流
	 * @param autoClose    是否自动关闭输出流
	 */
	public static void writeToXlsx(List<List<?>> rowList, OutputStream outputStream, boolean autoClose) {
		try (ExcelWriter writer = ExcelUtil.getWriter(true)) {
			if (CollUtil.isNotEmpty(rowList)) {
				for (List<?> objects : rowList) {
					writer.writeRow(objects);
				}
			}
			writer.flush(outputStream, autoClose);
		}
		log.info("Excel数据输出完成");
	}

	/**
	 * 将数据写入到Excel文件中，并写入输出流中,并根据类对象自动填充表头
	 *
	 * @param beanSupplier BeanSupplier
	 * @param contentList  数据列表
	 * @param outputStream 输出流
	 * @param autoClose    是否自动关闭输出流
	 */
	public static <T> void writeToXlsx(Supplier<T> beanSupplier, List<T> contentList, OutputStream outputStream, boolean autoClose) {
		Map<String, BiConsumer<T, Object>> mapping = createMapping(beanSupplier);
		List<String> header = new ArrayList<>();
		for (Map.Entry<String, BiConsumer<T, Object>> entry : mapping.entrySet()) {
			header.add(entry.getKey());
		}
		T instance = beanSupplier.get();
		var fieldSet = Arrays.stream(ReflectUtil.getFields(instance.getClass()))
				.filter(field -> field.isAnnotationPresent(ExcelHeader.class))
				.toList();
		List<List<?>> writeList = new ArrayList<>();
		writeList.add(header);
		for (T item : contentList) {
			List<Object> row = new ArrayList<>();
			for (Field field : fieldSet) {
				Method method = getGetMethod(item, field);
				// 通过方法获取字段值
				if (method != null) {
					Object returnValue = ReflectUtil.invoke(item, method);
					row.add(returnValue);
				}
				// 通过字段获取字段值
				else {
					Object fieldValue = ReflectUtil.getFieldValue(item, field);
					row.add(fieldValue);
				}
			}
			writeList.add(row);
		}
		writeToXlsx(writeList, outputStream, autoClose);
	}

	/**
	 * 将数据写入到Excel文件中，并写入输出流中,自动关闭输出流
	 *
	 * @param rowList      数据列表
	 * @param outputStream 输出流
	 */
	public static void writeToXlsx(List<List<?>> rowList, OutputStream outputStream) {
		writeToXlsx(rowList, outputStream, true);
	}

	/**
	 * 将数据写入到Excel文件中，并写入输出流中,并根据类对象自动填充表头,自动关闭输出流
	 *
	 * @param beanSupplier BeanSupplier
	 * @param contentList  数据列表
	 * @param outputStream 输出流
	 */
	public static <T> void writeToXlsx(Supplier<T> beanSupplier, List<T> contentList, OutputStream outputStream) {
		writeToXlsx(beanSupplier, contentList, outputStream, true);
	}

	/**
	 * 根据字段获取对应的Getter方法
	 *
	 * @param instance 字段所属对象
	 * @param field    字段
	 * @return Getter方法
	 */
	private static <T> Method getGetMethod(T instance, Field field) {
		String name = field.getName();
		Method getMethod = ReflectUtil.getMethodByName(instance.getClass(), "get" + StrUtil.upperFirst(name));
		if (getMethod == null) {
			// 为了兼容不规范的字段名，尝试获取小写的Getter方法
			getMethod = ReflectUtil.getMethodByName(instance.getClass(), "get" + name);
		}
		if (getMethod == null) {
			// 如果找不到对应的Getter方法，尝试获取以"get"开头的任意方法
			Method[] methods = ReflectUtil.getMethods(instance.getClass(), method -> method.getName().startsWith("get"));
			for (Method method : methods) {
				String lowerCase = method.getName().toLowerCase();
				// 全部转换为小写查找，如果包含目标字段名则认为找到了对应的Getter方法
				if (lowerCase.contains(name.toLowerCase())) {
					getMethod = method;
					break;
				}
			}
		}
		return getMethod;
	}
}
