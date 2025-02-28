package com.zhangfuxing.tools.excel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.sax.handler.RowHandler;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/2/25
 * @email zhangfuxing1010@163.com
 */
public class ExcelLoader {

	/**
	 * 创建一个BeanLoader对象，用于加载Excel文件并转换为JavaBean对象。
	 *
	 * @param beanSupplier 用于创建JavaBean对象的Supplier函数。
	 * @param <T>          JavaBean对象的类型。
	 * @return BeanLoader对象，用于加载Excel文件并转换为JavaBean对象。
	 */
	public static <T> BeanLoader<T> createBeanLoader(Supplier<T> beanSupplier) {
		return new BeanLoader<>(beanSupplier);
	}

	/**
	 * 从指定的 Excel 文件中加载数据，并使用指定的 Supplier 创建对象实例。
	 *
	 * @param filePath      Excel 文件路径
	 * @param beanSupplier  用于创建JavaBean对象的Supplier函数。
	 * @param columnMapping Excel列和JavaBean字段的映射关系。
	 * @param <T>           JavaBean对象的类型。
	 * @return 包含加载数据的对象列表。
	 */
	public static <T> List<T> beanLoader(String filePath, Supplier<T> beanSupplier, Map<String, BiConsumer<T, Object>> columnMapping) {
		return new BeanLoader<>(beanSupplier).addColumnMapping(columnMapping).load(filePath);
	}

	/**
	 * 从指定的 Excel 文件流中加载数据，并使用指定的 Supplier 创建对象实例。
	 *
	 * @param inputStream   Excel 文件流
	 * @param beanSupplier  用于创建JavaBean对象的Supplier函数。
	 * @param columnMapping Excel列和JavaBean字段的映射关系。
	 * @param <T>           JavaBean对象的类型。
	 * @return 包含加载数据的对象列表。
	 */
	public static <T> List<T> beanLoader(InputStream inputStream, Supplier<T> beanSupplier, Map<String, BiConsumer<T, Object>> columnMapping) {
		return new BeanLoader<>(beanSupplier).addColumnMapping(columnMapping).load(inputStream);
	}

	/**
	 * 从指定的 Excel 文件中加载数据，并使用指定的 Supplier 创建对象实例。
	 *
	 * @param file          Excel 文件
	 * @param beanSupplier  用于创建JavaBean对象的Supplier函数。
	 * @param columnMapping Excel列和JavaBean字段的映射关系。
	 * @param <T>           JavaBean对象的类型。
	 * @return 包含加载数据的对象列表。
	 */
	public static <T> List<T> beanLoader(File file, Supplier<T> beanSupplier, Map<String, BiConsumer<T, Object>> columnMapping) {
		return new BeanLoader<>(beanSupplier).addColumnMapping(columnMapping).load(file);
	}

	/**
	 * 从指定的 Excel 文件中加载数据，并使用指定的 Supplier 创建对象实例。
	 *
	 * @param filePath      Excel 文件路径
	 * @param beanSupplier  用于创建JavaBean对象的Supplier函数。
	 * @param columnMapping Excel列和JavaBean字段的映射关系。
	 * @param rid           Excel sheet 索引 -1 遍历所有sheet
	 * @param <T>           JavaBean对象的类型。
	 * @return 包含加载数据的对象列表。
	 */
	public static <T> List<T> beanLoader(String filePath, Supplier<T> beanSupplier, Map<String, BiConsumer<T, Object>> columnMapping, int rid) {
		return new BeanLoader<>(beanSupplier).addColumnMapping(columnMapping).load(filePath, rid);
	}

	/**
	 * 从指定的 Excel 文件流中加载数据，并使用指定的 Supplier 创建对象实例。
	 *
	 * @param inputStream   Excel 文件流
	 * @param beanSupplier  用于创建JavaBean对象的Supplier函数。
	 * @param columnMapping Excel列和JavaBean字段的映射关系。
	 * @param rid           Excel sheet 索引 -1 遍历所有sheet
	 * @param <T>           JavaBean对象的类型。
	 * @return 包含加载数据的对象列表。
	 */
	public static <T> List<T> beanLoader(InputStream inputStream, Supplier<T> beanSupplier, Map<String, BiConsumer<T, Object>> columnMapping, int rid) {
		return new BeanLoader<>(beanSupplier).addColumnMapping(columnMapping).load(inputStream, rid);
	}

	/**
	 * 从指定的 Excel 文件中加载数据，并使用指定的 Supplier 创建对象实例。
	 *
	 * @param file          Excel 文件
	 * @param beanSupplier  用于创建JavaBean对象的Supplier函数。
	 * @param columnMapping Excel列和JavaBean字段的映射关系。
	 * @param rid           Excel sheet 索引 -1 遍历所有sheet
	 * @param <T>           JavaBean对象的类型。
	 * @return 包含加载数据的对象列表。
	 */
	public static <T> List<T> beanLoader(File file, Supplier<T> beanSupplier, Map<String, BiConsumer<T, Object>> columnMapping, int rid) {
		return new BeanLoader<>(beanSupplier).addColumnMapping(columnMapping).load(file, rid);
	}

	/**
	 * 根据Java类创建Excel模板，并写入输出流中，默认关闭输出流
	 *
	 * @param outputStream 输出流
	 * @param mapping      列名与字段的映射关系
	 */
	public static <T> void writeTemplate(OutputStream outputStream, Map<String, BiConsumer<T, Object>> mapping) {
		writeTemplate(outputStream, mapping, true);
	}

	/**
	 * 根据Java类创建Excel模板，并写入输出流中
	 *
	 * @param outputStream 输出流
	 * @param mapping      列名与字段的映射关系
	 * @param autoClose    是否自动关闭输出流
	 */
	public static <T> void writeTemplate(OutputStream outputStream, Map<String, BiConsumer<T, Object>> mapping, boolean autoClose) {
		List<String> strings = new ArrayList<>();
		for (Map.Entry<String, BiConsumer<T, Object>> entry : mapping.entrySet()) {
			strings.add(entry.getKey());
		}
		writeTemplate(outputStream, strings, autoClose);
	}

	/**
	 * 根据Java类创建Excel模板，并写入输出流中
	 *
	 * @param outputStream 输出流
	 * @param headers      列名
	 * @param autoClose    是否自动关闭输出流
	 */
	public static <T> void writeTemplate(OutputStream outputStream, Iterable<T> headers, boolean autoClose) {
		try (ExcelWriter writer = ExcelUtil.getWriter(true)) {
			writer.writeRow(headers);
			writer.flush(outputStream, autoClose);
		}
	}

	/**
	 * 根据Java类创建Excel模板，并写入输出流中，默认关闭输出流
	 *
	 * @param outputStream 输出流
	 * @param headers      列名
	 */
	public static <T> void writeTemplate(OutputStream outputStream, Iterable<T> headers) {
		writeTemplate(outputStream, headers, true);
	}

	public static class BeanLoader<T> {
		Supplier<T> beanSupplier;
		Map<String, BiConsumer<T, Object>> columnMapping;

		public BeanLoader(Supplier<T> beanSupplier) {
			this.beanSupplier = beanSupplier;
			this.columnMapping = new HashMap<>();
		}

		/**
		 * 添加 excel列 和 java类字段 映射关系
		 * <p>
		 * 例：
		 * <p>
		 * ExcelColName   setter
		 * <p>
		 * "姓名"          (entity, cellValue) -> entity.setName(cellValue)
		 *
		 * @param columnName 列名
		 * @param setter     设置器
		 * @return BeanLoader
		 */
		public BeanLoader<T> addColumnMapping(String columnName, BiConsumer<T, Object> setter) {
			columnMapping.put(columnName, setter);
			return this;
		}

		/**
		 * 添加 excel列 和 java类字段 映射关系
		 *
		 * @param mapping 列名-设置器映射关系
		 * @return BeanLoader
		 */
		public BeanLoader<T> addColumnMapping(Map<String, BiConsumer<T, Object>> mapping) {
			if (mapping == null) {
				throw new IllegalArgumentException("mapping cannot be null");
			}
			columnMapping.putAll(mapping);
			return this;
		}

		/**
		 * 从指定路径的 Excel 文件中加载数据，并使用指定的 Supplier 创建对象实例。
		 * <p>
		 * 只读取第一个sheet内容
		 *
		 * @param filePath Excel 文件路径
		 * @return 包含加载数据的对象列表
		 */
		public List<T> load(String filePath) {
			return load(filePath, 0);
		}

		/**
		 * 从指定路径的 Excel 文件中加载数据，并使用指定的 Supplier 创建对象实例。
		 *
		 * @param filePath Excel 文件路径
		 * @param rid      Excel sheet 索引 -1 遍历所有sheet
		 * @return 包含加载数据的对象列表
		 */
		public List<T> load(String filePath, int rid) {
			List<T> result = new ArrayList<>();
			ExcelUtil.readBySax(filePath, rid, getRowHandler(result));
			return result;
		}

		/**
		 * 加载指定的excel文件并加载为Java Bean对象
		 *
		 * @param file excel文件对象
		 * @param rid  sheet索引 -1 表示读取所有sheet页
		 * @return Java Bean对象集合
		 */
		public List<T> load(File file, int rid) {
			List<T> result = new ArrayList<>();
			RowHandler rowHandler = getRowHandler(result);
			ExcelUtil.readBySax(file, rid, rowHandler);
			return result;
		}

		/**
		 * 加载指定的excel文件并加载为Java Bean对象
		 *
		 * @param file excel文件对象
		 * @return Java Bean对象集合
		 */
		public List<T> load(File file) {
			return load(file, 0);
		}

		/**
		 * 加载指定的excel文件流并加载为Java Bean对象
		 *
		 * @param inputStream excel文件流
		 * @param rid         sheet索引 -1 表示读取所有sheet页
		 * @return Java Bean对象集合
		 */
		public List<T> load(InputStream inputStream, int rid) {
			List<T> result = new ArrayList<>();
			RowHandler rowHandler = getRowHandler(result);
			ExcelUtil.readBySax(inputStream, rid, rowHandler);
			return result;
		}

		/**
		 * 加载指定的excel文件流并加载为Java Bean对象
		 *
		 * @param inputStream excel文件流
		 * @return Java Bean对象集合
		 */
		public List<T> load(InputStream inputStream) {
			return load(inputStream, 0);
		}

		/**
		 * 构建excel的行处理器，并将数据行转换的bean对象添加到结果集result中
		 *
		 * @param result 结果集
		 * @return 行处理器
		 */
		private RowHandler getRowHandler(List<T> result) {
			Map<String, Integer> headerIndex = new HashMap<>();
			final Set<String> headers = columnMapping.keySet();
			return (sheetIndex, rowIndex, rowList) -> {
				List<String> list = rowList.stream().map(Object::toString).toList();
				if (CollUtil.containsAny(headers, list)) {
					// 转换list为map
					Map<String, Integer> originMap = new HashMap<>();
					for (int i = 0; i < list.size(); i++) {
						originMap.put(list.get(i), i);
					}
					for (String header : headers) {
						Integer index = originMap.get(header);
						if (index != null) {
							headerIndex.put(header, index);
						}
					}
					return;
				}
				T instance = beanSupplier.get();
				for (var entry : columnMapping.entrySet()) {
					Integer index = headerIndex.get(entry.getKey());
					if (index != null) {
						entry.getValue().accept(instance, list.get(index));
					}
				}
				result.add(instance);
			};
		}

	}

}
