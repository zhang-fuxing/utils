package com.zhangfuxing.tools.converter;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 通过反射进行对象转换
 *
 * @author 张福兴
 * @version 1.0
 * @date 2025/7/23
 * @email zhangfuxing1010@163.com
 */
public class RefConv {

	@SuppressWarnings("unchecked")
	public static <T, R> Converter<T, R> create(@NotNull Class<T> sourceClass, @NotNull Supplier<R> targetInstanceSupplier, Rule<?, ?>... rules) {
		Class<R> targetClass = (Class<R>) targetInstanceSupplier.get().getClass();
		RefConvImpl<T, R> conv = new RefConvImpl<>(sourceClass, targetClass, targetInstanceSupplier);
		conv.rules(rules);
		return conv;
	}

	public static <T, R> Converter<T, R> create(@NotNull Class<T> sourceClass, @NotNull Class<R> targetClass, Rule<?, ?>... rules) {
		RefConvImpl<T, R> conv = new RefConvImpl<>(sourceClass, targetClass, null);
		conv.rules(rules);
		return conv;
	}

	public static <T, R> R conv(T value, Class<R> type, Rule<?, ?>... rules) {
		if (value == null || type == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Converter<T, R> converter = create((Class<T>) value.getClass(), type);
		converter.rules(rules);
		return converter.conv(value);
	}

	public static <T, R> R conv(T value, Class<R> type, Function<T, R> rule) {
		if (value == null || type == null) {
			return null;
		}
		//noinspection unchecked
		return conv(value, type, Rule.of((Class<T>) value.getClass(), type, rule));
	}

	public static <T, R> Converter<T, R> create(@NotNull Class<T> sourceClass, @NotNull Class<R> targetClass, Function<T, R> rule) {
		Converter<T, R> converter = create(sourceClass, targetClass);
		converter.rules(Rule.of(sourceClass, targetClass, rule));
		return converter;
	}

	public static <T, R> List<R> conv(List<T> sources, Class<R> type, Function<T, R> rule) {
		Class<T> sourceClass = null;
		if (sources != null && !sources.isEmpty()) {
			for (T source : sources) {
				if (source == null) {
					continue;
				}
				//noinspection unchecked
				sourceClass = (Class<T>) source.getClass();
			}
		}
		return conv(sources, type, Map.of(), Rule.of(sourceClass, type, rule));
	}

	public static <T, R> List<R> conv(List<T> sources, Class<R> type, Rule<?, ?>... rules) {
		return conv(sources, type, null, rules);
	}

	@SuppressWarnings("unchecked")
	public static <T, R> List<R> conv(List<T> sources, Class<R> type, Map<String, Function<T, ?>> maps, Rule<?, ?>... rules) {
		if (sources == null) {
			return Collections.emptyList();
		}
		List<R> result = new ArrayList<>();
		Converter<T, R> converter = null;
		for (T item : sources) {
			if (item == null) {
				continue;
			}
			if (converter == null) {
				converter = create((Class<T>) item.getClass(), type);
				converter.rules(rules);
				if (maps != null && !maps.isEmpty()) {
					maps.forEach(converter::map);
				}
			}
			R itemResult = converter.conv(item);
			result.add(itemResult);
		}
		return result;
	}


	static final class RefConvImpl<T, R> implements Converter<T, R> {

		private final Class<T> sourceClass;
		private final Class<R> targetClass;
		Map<Class<?>, Map<Class<?>, Function<?, ?>>> ruleTable;
		private final Map<String, Field> sourceClassFields;
		private final Map<String, Field> targetClassFields;
		private final Map<String, Function<T, ?>> fieldValueSuppliers;
		private final Supplier<R> targetInstanceSupplier;

		@SuppressWarnings("unchecked")
		public RefConvImpl(@NotNull Class<T> sourceClass, @NotNull Class<R> targetClass, Supplier<R> targetInstanceSupplier) {
			this.sourceClass = (Class<T>) toWrapperClass(sourceClass);
			this.targetClass = (Class<R>) toWrapperClass(targetClass);
			this.targetInstanceSupplier = targetInstanceSupplier;
			sourceClassFields = new HashMap<>();
			targetClassFields = new HashMap<>();
			fieldValueSuppliers = new ConcurrentHashMap<>();
			ruleTable = new ConcurrentHashMap<>(12);
			initDefaultRules();
			initFieldMaps();
		}

		private void initFieldMaps() {
			List<Field> sourceFields = getAllFields(sourceClass);
			List<Field> targetFields = getAllFields(targetClass);
			for (Field sourceField : sourceFields) {
				this.sourceClassFields.put(sourceField.getName(), sourceField);
			}
			for (Field targetField : targetFields) {
				this.targetClassFields.put(targetField.getName(), targetField);
			}
		}

		private List<Field> getAllFields(Class<?> clazz) {
			List<Field> fields = new ArrayList<>();
			while (clazz != null) {
				fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
				clazz = clazz.getSuperclass();
			}
			return fields;
		}


		@Override
		public R conv(T sourceInstance) {
			if (sourceInstance == null) {
				return null;
			}
			Conv<R> conv = simpleConv(sourceInstance);
			if (conv.ok) {
				return conv.result;
			}
			conv = complexConv(sourceInstance);

			return conv.ok ? conv.result : null;
		}

		@Override
		public void rules(Rule<?, ?>... rules) {
			for (Rule<?, ?> rule : rules) {
				if (rule.clazz() == null || rule.targetClass() == null || rule.rule() == null) {
					continue;
				}
				ruleTable.computeIfAbsent(rule.clazz(), k -> new ConcurrentHashMap<>())
						.put(rule.targetClass(), rule.rule());
			}
		}

		@Override
		public void map(String fieldName, Function<T, ?> fieldValue) {
			fieldValueSuppliers.put(fieldName, fieldValue);
		}

		private Conv<R> simpleConv(T t) {
			try {
				R result = targetClass.cast(t);
				return new Conv<>(true, result);
			} catch (Exception e) {
				return new Conv<>(false, null);
			}
		}

		private Conv<R> complexConv(T t) {
			// 基本类型转换
			Conv<R> conv = primitiveConv(t);
			if (conv.ok) {
				return conv;
			}
			// 引用类型转换
			return referenceConv(t);
		}

		private Conv<R> referenceConv(T input) {
			// map to object
			if (sourceClass.isAssignableFrom(Map.class) && !targetClass.isAssignableFrom(Map.class)) {
				return mapToObj(input);
			}

			// object to map
			if (!sourceClass.isAssignableFrom(Map.class) && targetClass.isAssignableFrom(Map.class)) {
				return objToMap(input);
			}

			R result = getTargetInstance();

			for (Map.Entry<String, Field> entry : this.targetClassFields.entrySet()) {
				String targetFieldName = entry.getKey();
				Field targetField = entry.getValue();
				Field sourceField = this.sourceClassFields.get(targetFieldName);
				Function<T, ?> supplier = this.fieldValueSuppliers.get(targetFieldName);
				Object value;
				if (sourceField == null && supplier == null) {
					continue;
				} else {
					value = supplier == null ? getFieldValue(sourceField, input) : supplier.apply(input);
				}
				setFieldValue(targetField, result, value);
			}
			return new Conv<>(true, result);
		}

		@SuppressWarnings("unchecked")
		private Conv<R> mapToObj(T input) {
			if (!(input instanceof Map)) {
				return new Conv<>(false, null);
			}
			R result = getTargetInstance();
			Map<String, ?> map = (Map<String, ?>) input;
			for (Map.Entry<String, Field> entry : this.targetClassFields.entrySet()) {
				String fieldName = entry.getKey();
				Field field = entry.getValue();
				Object value = map.get(fieldName);
				Function<T, ?> func = this.fieldValueSuppliers.get(fieldName);
				if (func != null) {
					value = func.apply(input);
				}
				if (value instanceof Map<?, ?> v &&
					!field.getType().isAssignableFrom(Map.class)) {
					value = RefConv.conv(v, field.getType());
				}
				setFieldValue(field, result, value);
			}
			return new Conv<>(true, result);
		}

		@SuppressWarnings("unchecked")
		private Conv<R> objToMap(T input) {
			if (input instanceof Map<?, ?>) {
				return new Conv<>(true, (R) input);
			}
			Map<String, Object> result = new LinkedHashMap<>(this.sourceClassFields.size(), 1F);
			for (Map.Entry<String, Field> entry : this.sourceClassFields.entrySet()) {
				String fieldName = entry.getKey();
				Field field = entry.getValue();
				Object value = getFieldValue(field, input);
				Function<T, ?> func = this.fieldValueSuppliers.get(fieldName);
				if (func != null) {
					value = func.apply(input);
				}
				result.put(fieldName, value);
			}
			return new Conv<>(true, (R) result);
		}

		private void setFieldValue(Field targetField, R result, Object value) {
			if (value == null) {
				return;
			}
			targetField.setAccessible(true);
			try {
				if (targetField.getType().isAssignableFrom(value.getClass())) {
					targetField.set(result, value);
				} else {
					Object conv = RefConv.conv(value, targetField.getType());
					targetField.set(result, conv);
				}
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("无法设置：" + this.targetClass + " 类的 " + targetField.getName() + " 字段值" + value, e);
			}
		}

		private Object getFieldValue(Field field, T input) {
			field.setAccessible(true);
			try {
				return field.get(input);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("无法获取：" + this.sourceClass + " 类的 " + field.getName() + " 字段的值", e);
			}
		}

		private Conv<R> primitiveConv(Object t) {
			if (!isPrimitiveOrWrapper(this.sourceClass)) {
				return new Conv<>(false, null);
			}
			Map<Class<?>, Function<?, ?>> ruleMap = this.ruleTable.get(this.sourceClass);
			Function<?, ?> function = ruleMap.get(this.targetClass);
			if (function != null) {
				//noinspection unchecked
				Function<Object, Object> typedFunction = (Function<Object, Object>) function;
				Object result = typedFunction.apply(t);
				//noinspection unchecked
				return new Conv<>(true, (R) result);
			}
			return new Conv<>(false, null);
		}

		private void initDefaultRules() {
			// 数字规则注册
			numberRuleRegister();
		}

		private R getTargetInstance() {
			R result;
			try {
				Constructor<R> targetConstructor = targetClass.getDeclaredConstructor();
				result = targetConstructor.newInstance();
			} catch (NoSuchMethodException e) {
				if (this.targetInstanceSupplier == null) {
					throw new IllegalArgumentException("无法实例化对象: " + targetClass + " 请提供有效无参构造或指定实例化提供者");
				}
				result = this.targetInstanceSupplier.get();
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new IllegalArgumentException("无法实例化对象: " + targetClass + " 请提供有效无参构造或指定实例化提供者", e);
			}
			return result;
		}

		private void numberRuleRegister() {
			Map<Class<?>, Function<?, ?>> convMap = getNumberRules();
			this.ruleTable.put(Byte.class, convMap);
			this.ruleTable.put(Short.class, convMap);
			this.ruleTable.put(Integer.class, convMap);
			this.ruleTable.put(Long.class, convMap);
			this.ruleTable.put(Float.class, convMap);
			this.ruleTable.put(Double.class, convMap);
			this.ruleTable.put(BigDecimal.class, convMap);
			this.ruleTable.put(BigInteger.class, convMap);
			this.ruleTable.put(String.class, convMap);
			this.ruleTable.put(Character.class, convMap);
			this.ruleTable.put(Boolean.class, convMap);
			this.ruleTable.put(Number.class, convMap);
		}

		private @NotNull Map<Class<?>, Function<?, ?>> getNumberRules() {
			Map<Class<?>, Function<?, ?>> convMap = new ConcurrentHashMap<>();
			convMap.put(Long.class, this::toLong);
			convMap.put(Integer.class, this::toInt);
			convMap.put(Double.class, this::toDouble);
			convMap.put(Float.class, this::toFloat);
			convMap.put(Short.class, this::toShort);
			convMap.put(Byte.class, this::toByte);
			convMap.put(Boolean.class, this::toBoolean);
			convMap.put(Character.class, this::toChar);
			convMap.put(String.class, this::toStr);
			return convMap;
		}

		private Integer toInt(Object obj) {
			return obj == null ? null : Integer.valueOf(obj.toString());
		}

		private Long toLong(Object obj) {
			return obj == null ? null : Long.valueOf(obj.toString());
		}

		private Float toFloat(Object obj) {
			return obj == null ? null : Float.valueOf(obj.toString());
		}

		private Double toDouble(Object obj) {
			return obj == null ? null : Double.valueOf(obj.toString());
		}

		private Short toShort(Object obj) {
			return obj == null ? null : Short.valueOf(obj.toString());
		}

		private Byte toByte(Object obj) {
			return obj == null ? null : Byte.valueOf(obj.toString());
		}

		private Character toChar(Object obj) {
			return obj == null ? null : (char) Integer.valueOf(obj.toString()).intValue();
		}

		private Boolean toBoolean(Object obj) {
			return obj == null ? null : Boolean.valueOf(obj.toString());
		}

		private String toStr(Object obj) {
			return obj == null ? null : obj.toString();
		}
	}

	// 将基本类型转换为包装类
	private static Class<?> toWrapperClass(Class<?> clazz) {
		if (clazz == int.class) return Integer.class;
		if (clazz == long.class) return Long.class;
		if (clazz == double.class) return Double.class;
		if (clazz == float.class) return Float.class;
		if (clazz == short.class) return Short.class;
		if (clazz == byte.class) return Byte.class;
		if (clazz == char.class) return Character.class;
		if (clazz == boolean.class) return Boolean.class;
		return clazz;
	}

	// 检查是否是基本类型或包装类
	private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		return clazz.isPrimitive() ||
			   Number.class.isAssignableFrom(clazz) ||
			   clazz == Boolean.class ||
			   clazz == Character.class;
	}

	private record Conv<E>(boolean ok, E result) {
	}
}
