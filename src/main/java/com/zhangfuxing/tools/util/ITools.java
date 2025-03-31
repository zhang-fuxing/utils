package com.zhangfuxing.tools.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections4.list.TreeList;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 常用工具类
 *
 * @author 张福兴
 * @version 1.0
 * @date 2025/3/18
 * @email zhangfuxing1010@163.com
 */
public class ITools {
	private final static String doubleFormat = "0.00";
	private final static DecimalFormat decimalFormat = new DecimalFormat(doubleFormat);

	/**
	 * 数字工具
	 */
	public static class NumberTools {
		public static String format(double number) {
			return decimalFormat.format(number);
		}

		public static String format(double number, String format) {
			DecimalFormat decimalFormat = new DecimalFormat(format);
			return decimalFormat.format(number);
		}

		public static String format(Double number) {
			if (null == number) {
				return "0";
			}
			return decimalFormat.format(number);
		}

		public static String format(Double number, String format) {
			if (null == number) {
				return "0";
			}
			if (null == format || format.isEmpty()) {
				format = doubleFormat;
			}
			DecimalFormat decimalFormat = new DecimalFormat(format);
			return decimalFormat.format(number);
		}

		public static String format(float number) {
			return decimalFormat.format(number);
		}

		public static String format(float number, String format) {
			DecimalFormat decimalFormat = new DecimalFormat(format);
			return decimalFormat.format(number);
		}

		public static String format(Float number) {
			if (null == number) {
				return "0";
			}
			return decimalFormat.format(number);
		}

		public static String format(Float number, String format) {
			if (null == number) {
				return "0";
			}
			if (null == format || format.isEmpty()) {
				format = doubleFormat;
			}
			DecimalFormat decimalFormat = new DecimalFormat(format);
			return decimalFormat.format(number);
		}

		public static <T extends java.lang.Number> T values(T number, T defaultValue) {
			if (null == number) {
				return defaultValue;
			}
			return number;
		}

		public static <T extends java.lang.Number> T values(T number) {
			return values(number, null);
		}

		public static BigDecimal add(java.lang.Number a, java.lang.Number b, java.lang.Number... others) {
			BigDecimal result = toBigDecimal(a).add(toBigDecimal(b));
			if (null != others) {
				for (java.lang.Number other : others) {
					result = result.add(toBigDecimal(other));
				}
			}
			return result;
		}

		public static BigDecimal subtract(java.lang.Number a, java.lang.Number b, java.lang.Number... others) {
			BigDecimal result = toBigDecimal(a).subtract(toBigDecimal(b));
			if (null != others) {
				for (java.lang.Number other : others) {
					result = result.subtract(toBigDecimal(other));
				}
			}
			return result;
		}

		public static BigDecimal multiply(java.lang.Number a, java.lang.Number b, java.lang.Number... others) {
			BigDecimal result = toBigDecimal(a).multiply(toBigDecimal(b));
			if (null != others) {
				for (java.lang.Number other : others) {
					result = result.multiply(toBigDecimal(other));
				}
			}
			return result;
		}

		public static BigDecimal divide(java.lang.Number a, java.lang.Number b, java.lang.Number... others) {
			return divide(RoundingMode.HALF_UP, a, b, others);
		}

		public static BigDecimal divide(RoundingMode roundingMode, java.lang.Number a, java.lang.Number b, java.lang.Number... others) {
			BigDecimal result = toBigDecimal(a).divide(toBigDecimal(b), 2, roundingMode);
			if (null != others) {
				for (java.lang.Number other : others) {
					result = result.divide(toBigDecimal(other), 2, roundingMode);
				}
			}
			return result;
		}

		public static BigDecimal toBigDecimal(Object input) {
			BigDecimal result;
			if (null == input) {
				result = BigDecimal.ZERO;
			} else if (input instanceof BigDecimal) {
				result = (BigDecimal) input;
			} else if (input instanceof Double number) {
				result = BigDecimal.valueOf(number);
			} else if (input instanceof Float number) {
				result = BigDecimal.valueOf(number);
			} else if (input instanceof Long number) {
				result = new BigDecimal(number);
			} else if (input instanceof Integer number) {
				result = new BigDecimal(number);
			} else if (input instanceof Short number) {
				result = new BigDecimal(number);
			} else if (input instanceof Byte number) {
				result = new BigDecimal(number);
			} else {
				result = new BigDecimal(input.toString());
			}
			return result;
		}
	}

	/**
	 * 字符串工具
	 */
	public static class StrTools {
		private static String templatePrefix = "${";
		private static String templateSuffix = "}";

		public static void setTemplatePrefix(String templatePrefix) {
			StrTools.templatePrefix = templatePrefix;
		}

		public static void setTemplateSuffix(String templateSuffix) {
			StrTools.templateSuffix = templateSuffix;
		}

		public static String toSmallCamel(String str) {
			return toSmallCamel(str, "_");
		}

		public static String toSmallCamel(String str, String regex) {
			if (null == str || str.isEmpty()) {
				return str;
			}
			String[] split = str.split(regex);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < split.length; i++) {
				String s = split[i];
				if (i == 0) {
					sb.append(s);
				} else {
					sb.append(s.substring(0, 1).toUpperCase()).append(s.substring(1));
				}
			}
			return sb.toString();
		}

		public static String toBigCamel(String str) {
			return toBigCamel(str, "_");
		}

		public static String toBigCamel(String str, String regex) {
			return toSmallCamel(str, regex).substring(0, 1).toUpperCase() + toSmallCamel(str, regex).substring(1);
		}

		public static String buildURL(String... urlParts) {
			if (null == urlParts || urlParts.length == 0) {
				return "";
			}
			StringBuilder sb = new StringBuilder();
			for (String urlPart : urlParts) {
				if (urlPart == null || urlPart.isEmpty()) {
					continue;
				}
				urlPart = urlPart.replace("\\", "/");
				if (urlPart.startsWith("/")) {
					urlPart = urlPart.substring(1);
				}
				if (urlPart.endsWith("/")) {
					urlPart = urlPart.substring(0, urlPart.length() - 1);
				}
				sb.append("/").append(urlPart);
			}
			return sb.toString();
		}

		public static String buildURL(Object... urlParts) {
			if (null == urlParts || urlParts.length == 0) {
				return "";
			}
			return buildURL(Arrays.stream(urlParts).map(String::valueOf).toArray(String[]::new));
		}

		public static boolean isBlank(String str) {
			if (null == str || str.isEmpty()) {
				return true;
			}
			for (int i = 0; i < str.length(); i++) {
				if (!Character.isWhitespace(str.charAt(i))) {
					return false;
				}
			}
			return true;
		}

		public static boolean isEmpty(String str) {
			return null == str || str.isEmpty();
		}

		public static String format(String template, Map<String, Object> map) {
			return format(template, templatePrefix, templateSuffix, map);
		}

		public static String format(String template, String prefix, String suffix, Map<String, Object> map) {
			if (isBlank(template) || map == null || map.isEmpty()) {
				return template;
			}

			// 转义正则特殊字符并构建模式
			String regex = "(?<!\\\\)"
						   + Pattern.quote(prefix)
						   + "(\\w+)"
						   + Pattern.quote(suffix);

			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(template);
			StringBuilder sb = new StringBuilder();

			while (matcher.find()) {
				String key = matcher.group(1);
				Object value = map.getOrDefault(key, "");
				matcher.appendReplacement(sb, Matcher.quoteReplacement(value.toString()));
			}
			matcher.appendTail(sb);

			// 处理转义符号
			String escapePattern = Pattern.quote("\\" + prefix);
			return sb.toString().replaceAll(escapePattern, prefix);
		}

		public static String iformat(String template, String prefix, String suffix, Object... params) {
			if (isBlank(template) || params == null || params.length == 0) {
				return template;
			}

			// 构建正则表达式匹配模式
			String regex = "(?<!\\\\)"
						   + Pattern.quote(prefix)
						   + Pattern.quote(suffix);

			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(template);
			StringBuilder sb = new StringBuilder();
			int paramIndex = 0;

			while (matcher.find()) {
				// 当参数不足时停止替换
				if (paramIndex >= params.length) {
					break;
				}

				String replacement = Matcher.quoteReplacement(params[paramIndex++].toString());
				matcher.appendReplacement(sb, replacement);
			}
			matcher.appendTail(sb);

			// 处理转义符号（将\prefix+suffix还原）
			String escapePattern = Pattern.quote("\\" + prefix) + Pattern.quote(suffix);
			return sb.toString().replaceAll(escapePattern, prefix + suffix);
		}

		public static String format(String template, Object... params) {
			return iformat(template, templatePrefix, templateSuffix, params);
		}

		public static String getFirstNotBlank(String... strs) {
			String result = null;
			if (null == strs) {
				result = "";
			} else {
				for (String str : strs) {
					if (!StrTools.isBlank(str)) {
						result = str;
						break;
					}
				}
				if (result == null) {
					result = "";
				}
			}
			return result;
		}

		public static String blankElseGet(String input, String defaultValue) {
			return StrTools.isBlank(input) ? defaultValue : input;
		}

		public static String emptyElseGet(String input, String defaultValue) {
			return StrTools.isEmpty(input) ? defaultValue : input;
		}

		public static String nullElseGet(String input, String defaultValue) {
			return null == input ? defaultValue : input;
		}

		public static String encodeUrl(String param) {
			return URLEncoder.encode(param, StandardCharsets.UTF_8);
		}

		public static String decodeUrl(String param) {
			return URLDecoder.decode(param, StandardCharsets.UTF_8);
		}

		public static int len(String str) {
			return str == null ? 0 : str.length();
		}

		public static String toString(Object obj) {
			return obj == null ? "" : obj.toString();
		}

		public static String connect(Collection<?> collection, String separator) {
			if (null == collection || collection.isEmpty()) {
				return "";
			}
			return collection.stream().map(StrTools::toString).collect(Collectors.joining(separator));
		}

		public static String connect(String separator, Object... objs) {
			if (null == objs || objs.length == 0) {
				return "";
			}
			return Arrays.stream(objs).map(StrTools::toString).collect(Collectors.joining(separator));
		}
	}

	/**
	 * 时间工具
	 */
	public static class DateTime {
		private static final String defFormat = "yyyy-MM-dd HH:mm:ss";
		private static final List<String> parseFormats;

		private final LocalDateTime value;
		private String format = defFormat;

		static {
			parseFormats = new ArrayList<>();
			// 初始化时按优先级添加格式（从最精确到最通用）
			String[] priorityFormats = {
					// 完整时间格式（含时区）
					"yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
					"yyyy-MM-dd HH:mm:ss.SSS Z",

					// ISO标准格式
					"yyyy-MM-dd HH:mm:ss",
					"yyyy/MM/dd HH:mm:ss",

					// 日期格式
					"yyyy-MM-dd",
					"yyyy/MM/dd",
					"yyyyMMdd",

					// 12小时制格式
					"yyyy-MM-dd hh:mm:ss a",

					// 本地化格式
					"dd-MMM-yyyy",
					"MMMM dd, yyyy",
					"yyyy年MM月dd日"
			};

			// 动态生成组合格式
			List<String> dynamicFormats = new ArrayList<>();
			String[] dateParts = {"yyyy-MM-dd", "yyyy/MM/dd"};
			String[] timeParts = {"HH:mm:ss", "HH:mm"};

			for (String date : dateParts) {
				for (String time : timeParts) {
					dynamicFormats.add(date + " " + time);
				}
			}

			// 合并并排序格式（按长度倒序）
			parseFormats.addAll(Arrays.asList(priorityFormats));
			parseFormats.addAll(dynamicFormats);
			parseFormats.sort((a, b) -> Integer.compare(b.length(), a.length()));

			// 添加其他补充格式
			Collections.addAll(parseFormats,
					"yyyy-MM",
					"yyyy/MM",
					"yyyyMM",
					"yyyy",
					"HH:mm:ss.SSSXXX",
					"HH:mm:ss.SSS Z",
					"hh:mm:ss a",
					"HH:mm:ss"
			);
		}

		public DateTime() {
			this.value = LocalDateTime.now();
		}

		public DateTime(long timestamp) {
			this.value = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
		}

		public DateTime(LocalDateTime value) {
			this.value = value;
		}

		public DateTime(LocalDate value) {
			this.value = LocalDateTime.of(value, LocalTime.of(0, 0, 0));
		}

		public DateTime(LocalTime value) {
			this.value = LocalDateTime.of(LocalDate.now(), value);
		}

		public DateTime(Date date) {
			this.value = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public int getYear() {
			return value.getYear();
		}

		public int getMonth() {
			return value.getMonthValue();
		}

		public int getDay() {
			return value.getDayOfMonth();
		}

		public int getHour() {
			return value.getHour();
		}

		public int getMinute() {
			return value.getMinute();
		}

		public int getSecond() {
			return value.getSecond();
		}

		public DateTime plusYears(int years) {
			return new DateTime(value.plusYears(years));
		}

		public DateTime plusMonths(int months) {
			return new DateTime(value.plusMonths(months));
		}

		public DateTime plusDays(int days) {
			return new DateTime(value.plusDays(days));
		}

		public DateTime plusHours(int hours) {
			return new DateTime(value.plusHours(hours));
		}

		public DateTime plusMinutes(int minutes) {
			return new DateTime(value.plusMinutes(minutes));
		}

		public DateTime plusSeconds(int seconds) {
			return new DateTime(value.plusSeconds(seconds));
		}

		public static DateTime now() {
			return new DateTime();
		}

		public static String format(long timestamp, String format) {
			DateTime dateTime = new DateTime(timestamp);
			dateTime.setFormat(format);
			return dateTime.toString();
		}

		public static String format(long timestamp) {
			return format(timestamp, defFormat);
		}

		public static String format(LocalDateTime value, String format) {
			DateTime dateTime = new DateTime(value);
			dateTime.setFormat(format);
			return dateTime.toString();
		}

		public static String format(LocalDateTime value) {
			return format(value, defFormat);
		}

		public static String format(LocalDate value, String format) {
			DateTime dateTime = new DateTime(value);
			dateTime.setFormat(format);
			return dateTime.toString();
		}

		public static String format(LocalDate value) {
			return format(value, defFormat);
		}

		public static String format(LocalTime value, String format) {
			DateTime dateTime = new DateTime(value);
			dateTime.setFormat(format);
			return dateTime.toString();
		}

		public static String format(LocalTime value) {
			return format(value, defFormat);
		}

		public static String format(Date date, String format) {
			DateTime dateTime = new DateTime(date);
			dateTime.setFormat(format);
			return dateTime.toString();
		}

		public static String format(Date date) {
			return format(date, defFormat);
		}

		public static String format(String format) {
			DateTime dateTime = new DateTime();
			dateTime.setFormat(format);
			return dateTime.toString();
		}

		public static String format() {
			return format(defFormat);
		}

		public static DateTime parse(String dateStr, String format) {
			DateTimeFormatter formatter = new DateTimeFormatterBuilder()
					.appendPattern(format)
					.parseDefaulting(ChronoField.YEAR, Year.now().getValue())
					.parseDefaulting(ChronoField.MONTH_OF_YEAR, LocalDate.now().getMonthValue())
					.parseDefaulting(ChronoField.DAY_OF_MONTH, LocalDate.now().getDayOfMonth())
					.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
					.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
					.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
					.toFormatter()
					.withLocale(Locale.getDefault())
					.withZone(ZoneId.systemDefault());

			TemporalAccessor temporal = formatter.parseBest(dateStr,
					LocalDateTime::from,
					LocalDate::from,
					YearMonth::from,
					LocalTime::from,
					Year::from
			);

			LocalDateTime result = null;
			if (temporal instanceof LocalDateTime) {
				result = (LocalDateTime) temporal;
			} else if (temporal instanceof LocalDate) {
				result = ((LocalDate) temporal).atStartOfDay();
			} else if (temporal instanceof YearMonth) {
				result = ((YearMonth) temporal).atDay(1).atStartOfDay();
			} else if (temporal instanceof Year) {
				result = ((Year) temporal).atMonth(1).atDay(1).atStartOfDay();
			}

			if (result != null) {
				return new DateTime(result);
			}
			throw new DateTimeParseException("日期解析错误：" + format, dateStr, 0);
		}

		public static DateTime parse(String dateStr) {
			for (String format : parseFormats) {
				try {
					return parse(dateStr, format);
				} catch (DateTimeParseException ignored) {
				}
			}

			throw new IllegalArgumentException("解析失败，输入: " + dateStr
											   + " 支持格式: " + parseFormats);
		}

		public static void addParseFormat(String format) {
			parseFormats.add(format);
		}

		@Override
		public String toString() {
			return value.format(DateTimeFormatter.ofPattern(format));
		}

		public Date getDate() {
			Instant instant = Instant.from(this.value)
					.atZone(ZoneId.systemDefault())
					.toInstant();
			return Date.from(instant);
		}

		public LocalDateTime toLocalDateTime() {
			return this.value;
		}

		public LocalDate toLocalDate() {
			return this.value.toLocalDate();
		}

		public LocalTime toLocalTime() {
			return this.value.toLocalTime();
		}
	}

	/**
	 * 集合工具类 依赖 PagesTools 进行分页辅助
	 */
	public static class CollTools {
		@SafeVarargs
		public static <T> List<T> toList(T... items) {
			return toList(ArrayList::new, items);
		}

		@SafeVarargs
		public static <T> List<T> toList(Supplier<List<T>> supplier, T... items) {
			if (supplier == null) {
				supplier = ArrayList::new;
			}
			List<T> list = supplier.get();
			Collections.addAll(list, items);
			return list;
		}

		@SafeVarargs
		public static <T> Set<T> toSet(T... items) {
			return toSet(HashSet::new, items);
		}

		@SafeVarargs
		public static <T> Set<T> toSet(Supplier<Set<T>> supplier, T... items) {
			if (supplier == null) {
				supplier = HashSet::new;
			}
			Set<T> set = supplier.get();
			Collections.addAll(set, items);
			return set;
		}

		public static <T extends Collection<?>> void removeNull(T collection) {
			if (collection != null) {
				collection.removeIf(Objects::isNull);
			}
		}

		public static <T> List<T> removeDuplicate(List<T> list) {
			if (list == null) {
				return Collections.emptyList();
			}
			return list.stream().distinct().collect(Collectors.toList());
		}

		@SafeVarargs
		@SuppressWarnings("unchecked")
		public static <T, C extends Collection<T>> C merge(C... lists) {
			if (lists == null || lists.length == 0) {
				return null;
			}
			return (C) Stream.of(lists).flatMap(Collection::stream).collect(Collectors.toCollection(() -> {
				try {
					return lists[0].getClass().getDeclaredConstructor().newInstance();
				} catch (Exception e) {
					return new ArrayList<T>();
				}
			}));
		}

		public static boolean isEmpty(Collection<?> collection) {
			return collection == null || collection.isEmpty();
		}

		public static <T> PagesTools<T> pages(PagesTools<T> pages) {
			if (pages == null || isEmpty(pages.getRecords())) {
				return new PagesTools<>();
			}
			List<T> records = pages.getRecords();
			int pageIndex = pages.getPageIndex();
			int pageSize = pages.getPageSize();
			return new PagesTools<>(
					records.subList((pageIndex - 1) * pageSize, Math.min(pageIndex * pageSize, records.size())),
					pageIndex,
					pageSize,
					records.size()
			);
		}

		public static <T> PagesTools<T> pages(List<T> records, int pageIndex, int pageSize) {
			PagesTools<T> pagesTools = new PagesTools<>(records, pageIndex, pageSize, size(records));
			return pages(pagesTools);
		}

		public static <T> List<T> subList(List<T> records, int pageIndex, int pageSize) {
			if (records == null) {
				return Collections.emptyList();
			}
			return records.subList((pageIndex - 1) * pageSize, Math.min(pageIndex * pageSize, records.size()));
		}

		public static int size(Collection<?> collection) {
			return collection == null ? 0 : collection.size();
		}

		@SuppressWarnings("unchecked")
		public static <T> T[] toArray(Collection<T> collection) {
			if (isEmpty(collection)) {
				return null;
			}
			return collection.toArray(new ArrayList<>(collection).toArray((T[]) Array.newInstance(collection.iterator().next().getClass(), 0)));
		}

		public static <T> String join(Iterable<T> iterable, String delimiter) {
			return join(iterable, Object::toString, delimiter);
		}

		public static <T> String join(Iterable<T> iterable, Function<T, String> converter, String delimiter) {
			return join(iterable, converter, delimiter, "", "");
		}

		public static <T> String join(Iterable<T> iterable, Function<T, String> converter) {
			return join(iterable, converter, ",", "", "");
		}

		public static <T> String join(Iterable<T> iterable, Function<T, String> converter, String delimiter, String prefix, String suffix) {
			if (iterable == null) {
				return "";
			}
			StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);
			for (T item : iterable) {
				String temp;
				if (converter != null) {
					temp = item == null ? "null" : converter.apply(item);
				} else {
					temp = item == null ? "null" : item.toString();
				}
				joiner.add(temp);
			}
			return joiner.toString();
		}

		public static <T> T getFirst(Iterable<T> iterable) {
			if (iterable == null) {
				return null;
			}
			Iterator<T> iterator = iterable.iterator();
			return iterator.hasNext() ? iterator.next() : null;
		}

		public static <T> T getLast(Iterable<T> iterable) {
			if (iterable == null) {
				return null;
			}
			if (iterable instanceof List<T> list) {
				return list.isEmpty() ? null : list.get(list.size() - 1);
			}
			Iterator<T> iterator = iterable.iterator();
			T last = null;
			while (iterator.hasNext()) {
				last = iterator.next();
			}
			return last;
		}
	}

	/**
	 * 分页工具类 依赖 CollTools 进行集合操作
	 */
	public static class PagesTools<T> {
		private int pageIndex;
		private int pageSize;
		private int total;
		private int totalPage;
		private List<T> records;

		public PagesTools() {
			this.pageIndex = 1;
			this.pageSize = 100;
			this.total = 0;
			this.totalPage = 0;
		}

		public PagesTools(int pageIndex, int pageSize, int total) {
			this.pageIndex = pageIndex;
			this.pageSize = pageSize;
			this.total = total;
			this.totalPage = (int) Math.ceil((double) total / pageSize);
		}

		public PagesTools(List<T> records) {
			this.records = records;
			this.pageIndex = 1;
			this.pageSize = 100;
			this.total = CollTools.size(records);
			this.totalPage = (int) Math.ceil((double) total / pageSize);
		}

		public PagesTools(List<T> records, int pageIndex, int pageSize, int totalSize) {
			this.records = records;
			this.pageIndex = pageIndex;
			this.pageSize = pageSize;
			this.total = totalSize;
			this.totalPage = (int) Math.ceil((double) totalSize / pageSize);
		}

		public List<T> getRecords() {
			return records;
		}

		public void setRecords(List<T> records) {
			this.records = records;
		}

		public int getPageIndex() {
			return pageIndex;
		}

		public void setPageIndex(int pageIndex) {
			this.pageIndex = pageIndex;
		}

		public int getPageSize() {
			return pageSize;
		}

		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
		}

		public int getTotal() {
			return total;
		}

		public void setTotal(int total) {
			this.total = total;
		}

		public int getTotalPage() {
			return totalPage;
		}

		public void setTotalPage(int totalPage) {
			this.totalPage = totalPage;
		}

		@Override
		public String toString() {
			return "IPages{" +
				   "pageIndex=" + pageIndex +
				   ", pageSize=" + pageSize +
				   ", total=" + total +
				   ", totalPage=" + totalPage +
				   ", records=" + records +
				   '}';
		}
	}

	/**
	 * 摘要工具类
	 */
	public static class DigestTools {
		public static String md5(String content) {
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(content.getBytes());
				return new BigInteger(1, md.digest()).toString(16);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		public static String sha1(String content) {
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				md.update(content.getBytes());
				return new BigInteger(1, md.digest()).toString(16);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		public static String sha256(String content) {
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update(content.getBytes());
				return new BigInteger(1, md.digest()).toString(16);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		public static String md5(byte[] content) {
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(content);
				return new BigInteger(1, md.digest()).toString(16);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		public static String sha1(byte[] content) {
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				md.update(content);
				return new BigInteger(1, md.digest()).toString(16);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		public static String sha256(byte[] content) {
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update(content);
				return new BigInteger(1, md.digest()).toString(16);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		public static String md5(InputStream inputStream) {
			try (inputStream) {
				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] buffer = new byte[8192];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					md.update(buffer, 0, bytesRead);
				}
				return new BigInteger(1, md.digest()).toString(16);
			} catch (NoSuchAlgorithmException | IOException e) {
				throw new RuntimeException(e);
			}
		}

		public static String md5(File file) {
			try {
				return md5(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 加密工具类
	 */
	public static class Encrypt {
		public static String base64Encode(String string) {
			return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
		}

		public static String base64Decode(String base64) {
			return new String(Base64.getDecoder().decode(base64));
		}

		public static String encByAES(String content, String key, boolean hex) {
			try {
				Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
				SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
				cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
				byte[] bytes = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
				return hex ? Hex.encodeHexString(bytes) : Base64.getEncoder().encodeToString(bytes);
			} catch (Exception e) {
				throw new RuntimeException("加密失败", e);
			}
		}

		public static String decByAES(String content, String key, boolean hex) {
			try {
				Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
				SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
				cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
				byte[] bytes = cipher.doFinal(hex ? Hex.decodeHex(content) : Base64.getDecoder().decode(content));
				return new String(bytes, StandardCharsets.UTF_8);
			} catch (Exception e) {
				throw new RuntimeException("解密失败", e);
			}
		}

		public static String encByAES(byte[] bytes, byte[] key, boolean hex) {
			try {
				Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
				SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
				cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
				return hex ? Hex.encodeHexString(cipher.doFinal(bytes)) : Base64.getEncoder().encodeToString(cipher.doFinal(bytes));
			} catch (Exception e) {
				throw new RuntimeException("加密失败", e);
			}
		}

		public static byte[] decByAES(String content, byte[] key, boolean hex) {
			try {
				Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
				SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
				cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
				return hex ? Hex.decodeHex(content) : Base64.getDecoder().decode(content);
			} catch (Exception e) {
				throw new RuntimeException("解密失败", e);
			}
		}
	}

	/**
	 * 树工具类
	 */
	public static class TreeTools {
		public static <T> Source<T> builder(Collection<T> collection) {
			return new Source<>(collection);
		}

		public static final class Source<T> {
			private final Collection<T> source;

			public Source(Collection<T> source) {
				this.source = source;
			}

			/**
			 * 当前节点是否是根节点
			 *
			 * @param rootPredicate 根节点判断
			 * @return RootElement
			 */
			public RootElement<T> isRootEl(Predicate<T> rootPredicate) {
				return new RootElement<>(source, rootPredicate);
			}

		}

		public static final class RootElement<T> {
			private final Collection<T> source;
			private final Predicate<T> rootPredicate;

			public RootElement(Collection<T> source, Predicate<T> rootPredicate) {
				this.source = source;
				this.rootPredicate = rootPredicate;
			}

			/**
			 * 提取出元素id
			 *
			 * @param extractIdFunc 提取id方法
			 * @return IdRecord
			 */
			public IdRecord<T> extractId(Function<T, ?> extractIdFunc) {
				return new IdRecord<>(source, rootPredicate, extractIdFunc);
			}

		}

		public static final class IdRecord<T> {
			private final Collection<T> source;
			private final Predicate<T> rootPredicate;
			private final Function<T, ?> extractIdFunc;

			public IdRecord(Collection<T> source,
							Predicate<T> rootPredicate,
							Function<T, ?> extractIdFunc) {
				this.source = source;
				this.rootPredicate = rootPredicate;
				this.extractIdFunc = extractIdFunc;
			}

			/**
			 * 提取出元素父id
			 *
			 * @param extractParentIdFunc 提取父id方法
			 * @return ParentRecord
			 */
			public ParentRecord<T> extractParent(Function<T, ?> extractParentIdFunc) {
				return new ParentRecord<>(source, rootPredicate, extractIdFunc, extractParentIdFunc);
			}

		}

		public static final class ParentRecord<T> {
			private final Collection<T> source;
			private final Predicate<T> rootPredicate;
			private final Function<T, ?> extractIdFunc;
			private final Function<T, ?> extractParentIdFunc;

			public ParentRecord(Collection<T> source,
								Predicate<T> rootPredicate,
								Function<T, ?> extractIdFunc,
								Function<T, ?> extractParentIdFunc) {
				this.source = source;
				this.rootPredicate = rootPredicate;
				this.extractIdFunc = extractIdFunc;
				this.extractParentIdFunc = extractParentIdFunc;
			}

			/**
			 * 将当前元素添加到父元素的children元素中
			 *
			 * @param addChildFunc 添加子节点方法
			 * @return ChildRecord
			 */
			public ChildRecord<T> addChild(BiConsumer<T, T> addChildFunc) {
				return new ChildRecord<>(source, rootPredicate, extractIdFunc, extractParentIdFunc, addChildFunc);
			}

		}

		public static final class ChildRecord<T> {
			private final Collection<T> source;
			private final Predicate<T> rootPredicate;
			private final Function<T, ?> extractIdFunc;
			private final Function<T, ?> extractParentIdFunc;
			private final BiConsumer<T, T> addChildFunc;

			public ChildRecord(Collection<T> source,
							   Predicate<T> rootPredicate,
							   Function<T, ?> extractIdFunc,
							   Function<T, ?> extractParentIdFunc,
							   BiConsumer<T, T> addChildFunc) {
				this.source = source;
				this.rootPredicate = rootPredicate;
				this.extractIdFunc = extractIdFunc;
				this.extractParentIdFunc = extractParentIdFunc;
				this.addChildFunc = addChildFunc;
			}

			/**
			 * 构建出树形列表
			 *
			 * @return Collection
			 */
			public Collection<T> build() {
				return buildTree(source, ArrayList::new, rootPredicate, extractIdFunc, extractParentIdFunc, addChildFunc, true);
			}

			/**
			 * 构建树， 默认返回ArrayList， 返回类型可自定义
			 *
			 * @param listType            1: TreeList, 2: LinkedList, 3: CopyOnWriteArrayList other: ArrayList
			 * @param noneParentAddToRoot 是否将没有父节点的元素添加到根节点
			 * @return List
			 */
			public List<T> buildAsList(int listType, boolean noneParentAddToRoot) {
				return switch (listType) {
					case 1 ->
							new TreeList<>(buildTree(source, TreeList::new, rootPredicate, extractIdFunc, extractParentIdFunc, addChildFunc, noneParentAddToRoot));
					case 2 ->
							new LinkedList<>(buildTree(source, LinkedList::new, rootPredicate, extractIdFunc, extractParentIdFunc, addChildFunc, noneParentAddToRoot));
					case 3 ->
							new CopyOnWriteArrayList<>(buildTree(source, CopyOnWriteArrayList::new, rootPredicate, extractIdFunc, extractParentIdFunc, addChildFunc, noneParentAddToRoot));
					default ->
							new ArrayList<>(buildTree(source, ArrayList::new, rootPredicate, extractIdFunc, extractParentIdFunc, addChildFunc, noneParentAddToRoot));
				};
			}

			/**
			 * 构建树，返回 ArrayList
			 *
			 * @param noneParentAddToRoot 是否将没有父节点的元素添加到根节点
			 * @return ArrayList
			 */
			public List<T> buildAsList(boolean noneParentAddToRoot) {
				return buildAsList(0, noneParentAddToRoot);
			}

			/**
			 * 构建树，返回 ArrayList， 默认将没有父节点的元素添加到根节点
			 *
			 * @return ArrayList
			 */
			public List<T> buildAsList() {
				return buildAsList(0, true);
			}

			/**
			 * 构建树， 默认返回HashSet， 返回类型可自定义
			 *
			 * @param setType             1: TreeSet, 2: LinkedHashSet, 3: CopyOnWriteArraySet other: HashSet
			 * @param noneParentAddToRoot 是否将没有父节点的元素添加到根节点
			 * @return Set
			 */
			public Set<T> buildAsSet(int setType, boolean noneParentAddToRoot) {
				return switch (setType) {
					case 1 ->
							new TreeSet<>(buildTree(source, TreeSet::new, rootPredicate, extractIdFunc, extractParentIdFunc, addChildFunc, noneParentAddToRoot));
					case 2 ->
							new LinkedHashSet<>(buildTree(source, LinkedHashSet::new, rootPredicate, extractIdFunc, extractParentIdFunc, addChildFunc, noneParentAddToRoot));
					case 3 ->
							new CopyOnWriteArraySet<>(buildTree(source, CopyOnWriteArraySet::new, rootPredicate, extractIdFunc, extractParentIdFunc, addChildFunc, noneParentAddToRoot));
					default ->
							new HashSet<>(buildTree(source, HashSet::new, rootPredicate, extractIdFunc, extractParentIdFunc, addChildFunc, noneParentAddToRoot));
				};
			}

			/**
			 * 构建树，返回 HashSet
			 *
			 * @param noneParentAddToRoot 是否将没有父节点的元素添加到根节点
			 * @return HashSet
			 */
			public Set<T> buildAsSet(boolean noneParentAddToRoot) {
				return buildAsSet(0, noneParentAddToRoot);
			}

			/**
			 * 构建树，返回 HashSet， 默认将没有父节点的元素添加到根节点
			 *
			 * @return HashSet
			 */
			public Set<T> buildAsSet() {
				return buildAsSet(0, true);
			}
		}

		private static <T> Collection<T> buildTree(Collection<T> source,
												   Supplier<Collection<T>> rootSupplier,
												   Predicate<T> isRootItem,
												   Function<T, ?> uniqueKey,
												   Function<T, ?> parentKey,
												   BiConsumer<T, T> addChild,
												   boolean noneParentAddToRoot) {
			Collection<T> root = rootSupplier.get();
			if (source == null || source.isEmpty()) {
				return root;
			}
			Map<Object, T> map = new HashMap<>();
			for (T item : source) {
				Object apply = uniqueKey.apply(item);
				map.put(apply, item);
			}

			for (T item : source) {
				if (isRootItem.test(item)) {
					root.add(item);
					continue;
				}
				Object parentId = parentKey.apply(item);
				T parent = map.get(parentId);
				if (parent == null) {
					if (noneParentAddToRoot) {
						root.add(item);
					}
					continue;
				}
				addChild.accept(parent, item);
			}
			return root;
		}

	}

	/**
	 * 反射工具类
	 * <p>
	 * 依赖TypeTools进行类型转换，
	 * <p>
	 * 依赖NULL进行参数包装
	 */
	public static class RefTools {

		/**
		 * 创建类实例 需要该类可以进行实例化，如果没有无参构造函数，那么需要注意构造时传入的参数，
		 * <p>
		 * 构造参数如果为 null，需要使用 NULL(Class.class)来避免类型丢失导致无法获取到对应构造函数导致实例化失败
		 *
		 * @param clazz 需要创建实例的类
		 * @param args  构造函数的参数
		 * @param <T>   返回的实例类型
		 * @return 实例对象
		 */
		@SuppressWarnings("unchecked")
		public static <T> T newInstance(Class<T> clazz, Object... args) {
			try {
				return (T) MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
						.findConstructor(clazz, MethodType.methodType(void.class, getParameterTypes(args)))
						.invokeWithArguments(unwrap(args));
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * 调用静态方法
		 *
		 * @param clazz      方法所属类
		 * @param methodName 方法名
		 * @param returnType 返回类型
		 * @param args       参数：如果参数为null， 需要使用 NULL(Class.class)来避免类型丢失导致无法获取正确的方法
		 * @param <T>        类类型
		 * @param <R>        返回值类型
		 * @return 返回值
		 */
		@SuppressWarnings("unchecked")
		public static <T, R> R invoke(Class<T> clazz, String methodName, Class<?> returnType, Object... args) {
			try {
				Object o = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
						.findStatic(clazz, methodName, MethodType.methodType(returnType, getParameterTypes(args)))
						.invokeWithArguments(unwrap(args));
				return (R) o;
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * 调用静态方法
		 *
		 * @param clazz  方法所属类
		 * @param method 方法对象
		 * @param args   参数：如果参数为null， 需要使用 NULL(Class.class)来避免类型丢失导致无法获取正确的方法
		 * @param <T>    类类型
		 * @param <R>    返回值类型
		 * @return 返回值
		 */
		@SuppressWarnings("unchecked")
		public static <T, R> R invoke(Class<T> clazz, Method method, Object... args) {
			try {
				return (R) MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
						.unreflectSpecial(method, clazz)
						.invokeWithArguments(unwrap(args));
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * 调用实例方法
		 *
		 * @param instance 实例对象
		 * @param method   method对象
		 * @param args     参数：如果参数为null， 需要使用 NULL(Class.class)来避免类型丢失导致无法获取正确的方法
		 * @param <T>      实例对象
		 * @param <R>      返回值类型
		 * @return 返回值
		 */
		@SuppressWarnings("unchecked")
		public static <T, R> R invoke(T instance, Method method, Object... args) {
			try {
				Class<?> clazz = instance.getClass();
				return (R) MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
						.unreflectSpecial(method, clazz)
						.bindTo(instance)
						.invokeWithArguments(args);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * 调用实例方法
		 *
		 * @param instance   实例对象
		 * @param methodName 方法名称
		 * @param returnType 返回类型
		 * @param args       参数：如果参数为null， 需要使用 NULL(Class.class)来避免类型丢失导致无法获取正确的方法
		 * @param <T>        实例对象
		 * @param <R>        返回值类型
		 * @return 返回值
		 */
		@SuppressWarnings("unchecked")
		public static <T, R> R invoke(T instance, String methodName, Class<?> returnType, Object... args) {
			try {
				Object res = MethodHandles.privateLookupIn(instance.getClass(), MethodHandles.lookup())
						.findVirtual(instance.getClass(), methodName, MethodType.methodType(returnType, getParameterTypes(args)))
						.bindTo(instance)
						.invokeWithArguments(unwrap(args));

				return (R) res;
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * 获取类中所有字段
		 *
		 * @param clazz 类
		 * @return 字段列表
		 */
		public static List<Field> getFields(Class<?> clazz) {
			return getFields(clazz, true, false);
		}

		/**
		 * 获取类中所有字段
		 *
		 * @param clazz         类
		 * @param superPriority 父类字段优先级
		 * @return 字段列表
		 */
		public static List<Field> getFields(Class<?> clazz, boolean superPriority) {
			return getFields(clazz, true, superPriority);
		}

		/**
		 * 获取类中所有字段
		 *
		 * @param clazz              类
		 * @param containsSuperField 是否包含父类字段
		 * @param superPriority      父类字段优先级
		 * @return 字段列表
		 */
		public static List<Field> getFields(Class<?> clazz, boolean containsSuperField, boolean superPriority) {
			try {
				List<Class<?>> classes = new ArrayList<>();
				if (containsSuperField) {
					while (clazz != null) {
						classes.add(clazz);
						clazz = clazz.getSuperclass();
					}
				} else {
					classes.add(clazz);
				}
				if (superPriority)
					Collections.reverse(classes);
				return classes.stream()
						.flatMap(c -> Arrays.stream(c.getDeclaredFields()))
						.collect(Collectors.toList());

			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		public static List<Method> getMethods(Class<?> clazz) {
			return getMethods(clazz, true);
		}

		public static List<Method> getMethods(Class<?> clazz, boolean containsSuperMethod) {
			return getMethods(clazz, containsSuperMethod, false);
		}

		public static List<Method> getMethods(Class<?> clazz, boolean containsSuperMethod, boolean superPriority) {
			return getMethods(clazz, containsSuperMethod, superPriority, false);
		}

		public static List<Method> getMethods(Class<?> clazz, boolean containsSuperMethod, boolean superPriority, boolean objectMethod) {
			try {
				List<Class<?>> classes = new ArrayList<>();
				if (containsSuperMethod) {
					while (clazz != null) {
						if (!objectMethod && clazz == Object.class) {
							break;
						}
						classes.add(clazz);
						clazz = clazz.getSuperclass();
					}
				} else {
					classes.add(clazz);
				}
				if (superPriority)
					Collections.reverse(classes);
				return classes.stream()
						.flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
						.collect(Collectors.toList());
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		public static <T> void setFieldValue(T instance, Field field, Object value) {
			try {
				MethodHandles.privateLookupIn(instance.getClass(), MethodHandles.lookup())
						.unreflectSetter(field)
						.invoke(instance, TypeTools.cast(value, field.getType()));
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		public static <T> Object getFieldValue(T instance, Field field) {
			try {
				return MethodHandles.privateLookupIn(instance.getClass(), MethodHandles.lookup())
						.unreflectGetter(field)
						.invoke(instance);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * 解包null
		 *
		 * @param args 参数
		 * @return 解包后的参数
		 */
		private static Object[] unwrap(Object... args) {
			return Arrays.stream(args)
					.map(o -> {
						if (o instanceof NULL<?>) {
							return null;
						} else {
							return o;
						}
					})
					.toArray();
		}

		/**
		 * 获取参数类型
		 *
		 * @param args 参数
		 * @return 参数类型
		 */
		private static Class<?>[] getParameterTypes(Object... args) {
			return Arrays.stream(args)
					.map(o -> {
						if (o == null) {
							return Object.class;
						} else if (o instanceof NULL<?> nullObj) {
							return nullObj.clazz();
						} else {
							return o.getClass();
						}
					})
					.toArray(Class[]::new);
		}

		public static <T> Field getField(Class<T> clazz, String fieldName) {
			List<Field> fields = getFields(clazz);
			return fields.stream()
					.filter(f -> f.getName().equals(fieldName))
					.findFirst()
					.orElse(null);
		}

		public static Method getMethod(Class<?> clazz, String methodName) {
			List<Method> methods = getMethods(clazz);
			return methods.stream()
					.filter(m -> m.getName().equals(methodName))
					.findFirst()
					.orElse(null);
		}
	}

	/**
	 * 类型转换工具 依赖 DateTime 进行日期解析
	 */
	public static class TypeTools {
		/**
		 * 通用类型转换方法（支持基础类型、包装类、日期等常见类型转换）
		 *
		 * @param instance 原始对象
		 * @param clazz    目标类型
		 * @param <T>      原始类型
		 * @param <R>      目标类型
		 * @return 转换后的对象
		 */
		public static <T, R> R cast(T instance, Class<R> clazz) {
			if (instance == null) return null;
			if (clazz.isInstance(instance)) {
				return clazz.cast(instance);
			}

			Class<?> sourceType = instance.getClass();

			try {
				// 处理基础数据类型转换
				if (clazz.isPrimitive()) {
					return castToPrimitive(instance, clazz);
				}

				// 处理数字类型转换
				if (Number.class.isAssignableFrom(clazz)) {
					return convertNumber(instance, clazz);
				}

				// 处理日期类型转换
				if (Date.class.isAssignableFrom(clazz)) {
					return convertDate(instance, clazz);
				}
				if (LocalDateTime.class.isAssignableFrom(clazz) ||
					LocalDate.class.isAssignableFrom(clazz) ||
					LocalTime.class.isAssignableFrom(clazz)) {
					return convertDateTime(instance, clazz);
				}

				// 处理字符串转换
				if (clazz == String.class) {
					return clazz.cast(instance.toString());
				}

				// 处理布尔类型转换
				if (clazz == Boolean.class || clazz == boolean.class) {
					return convertBoolean(instance, clazz);
				}

				// 尝试使用目标类型的构造函数
				try {
					Constructor<R> constructor = clazz.getConstructor(sourceType);
					return constructor.newInstance(instance);
				} catch (NoSuchMethodException ignored) {
				}

				// 尝试使用valueOf方法
				try {
					Method valueOf = clazz.getMethod("valueOf", sourceType);
					return clazz.cast(valueOf.invoke(null, instance));
				} catch (NoSuchMethodException ignored) {
				}

				// 尝试使用parse方法
				try {
					Method parse = clazz.getMethod("parse", CharSequence.class);
					return clazz.cast(parse.invoke(null, instance.toString()));
				} catch (NoSuchMethodException ignored) {
				}

			} catch (Exception e) {
				throw new ClassCastException("Type conversion failed: " + e.getMessage());
			}

			throw new ClassCastException("Cannot cast " + sourceType.getName() + " to " + clazz.getName());
		}

		private static <R, T> R convertDateTime(T instance, Class<R> clazz) {
			if (instance instanceof TemporalAccessor temporalAccessor) {
				if (clazz == LocalDateTime.class) {
					return clazz.cast(LocalDateTime.from(temporalAccessor));
				} else if (clazz == LocalDate.class) {
					return clazz.cast(LocalDate.from(temporalAccessor));
				} else if (clazz == LocalTime.class) {
					return clazz.cast(LocalTime.from(temporalAccessor));
				}
			} else if (instance instanceof String str) {
				DateTime parse = DateTime.parse(str);
				if (clazz == LocalDateTime.class) {
					return clazz.cast(parse.toLocalDateTime());
				} else if (clazz == LocalDate.class) {
					return clazz.cast(parse.toLocalDate());
				} else if (clazz == LocalTime.class) {
					return clazz.cast(parse.toLocalTime());
				}
			} else if (instance instanceof Date) {
				if (clazz == LocalDateTime.class) {
					return clazz.cast(LocalDateTime.ofInstant(((Date) instance).toInstant(), ZoneId.systemDefault()));
				} else if (clazz == LocalDate.class) {
					return clazz.cast(LocalDate.ofInstant(((Date) instance).toInstant(), ZoneId.systemDefault()));
				} else if (clazz == LocalTime.class) {
					return clazz.cast(LocalTime.ofInstant(((Date) instance).toInstant(), ZoneId.systemDefault()));
				}
			} else if (instance instanceof Calendar) {
				if (clazz == LocalDateTime.class) {
					return clazz.cast(LocalDateTime.ofInstant(((Calendar) instance).toInstant(), ZoneId.systemDefault()));
				} else if (clazz == LocalDate.class) {
					return clazz.cast(LocalDate.ofInstant(((Calendar) instance).toInstant(), ZoneId.systemDefault()));
				} else if (clazz == LocalTime.class) {
					return clazz.cast(LocalTime.ofInstant(((Calendar) instance).toInstant(), ZoneId.systemDefault()));
				}
			} else if (instance instanceof Long timestamp) {
				if (clazz == LocalDateTime.class) {
					return clazz.cast(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
				} else if (clazz == LocalDate.class) {
					return clazz.cast(LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
				} else if (clazz == LocalTime.class) {
					return clazz.cast(LocalTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
				}
			}
			throw new ClassCastException("Cannot cast " + instance.getClass().getName() + " to " + clazz.getName());
		}

		// region 辅助方法
		private static <R> R convertNumber(Object value, Class<R> targetType) {
			Number number;
			if (value instanceof Number) {
				number = (Number) value;
			} else if (value instanceof Boolean) {
				number = ((Boolean) value) ? 1 : 0;
			} else {
				number = parseStringToNumber(value.toString());
			}

			if (targetType == Integer.class || targetType == int.class) {
				return targetType.cast(number.intValue());
			} else if (targetType == Long.class || targetType == long.class) {
				return targetType.cast(number.longValue());
			} else if (targetType == Double.class || targetType == double.class) {
				return targetType.cast(number.doubleValue());
			} else if (targetType == Float.class || targetType == float.class) {
				return targetType.cast(number.floatValue());
			} else if (targetType == BigDecimal.class) {
				return targetType.cast(new BigDecimal(number.toString()));
			}
			throw new NumberFormatException("Unsupported number type: " + targetType.getName());
		}

		private static Number parseStringToNumber(String str) {
			if (str.contains(".")) {
				try {
					return Double.parseDouble(str);
				} catch (NumberFormatException e) {
					throw new NumberFormatException("Invalid numeric format: " + str);
				}
			}
			try {
				return Long.parseLong(str);
			} catch (NumberFormatException e) {
				return new BigInteger(str);
			}
		}

		@SuppressWarnings("unchecked")
		private static <R> R convertDate(Object value, Class<R> targetType) {
			if (value instanceof TemporalAccessor temporal) {
				Instant instant = Instant.from(temporal);
				if (targetType == Date.class) {
					return (R) Date.from(instant);
				}
			} else if (value instanceof Date date) {
				return (R) date;
			} else if (value instanceof String str) {
				DateTime parse = DateTime.parse(str);
				return (R) parse.getDate();
			} else if (value instanceof Calendar calendar) {
				return (R) calendar.getTime();
			} else if (value instanceof Long timestamp) {
				return (R) Date.from(Instant.ofEpochMilli(timestamp));
			}
			throw new DateTimeException("Unsupported date conversion");
		}

		@SuppressWarnings("unchecked")
		private static <R> R castToPrimitive(Object value, Class<R> targetType) {
			if (targetType == int.class) return (R) Integer.valueOf(value.toString());
			if (targetType == long.class) return (R) Long.valueOf(value.toString());
			if (targetType == double.class) return (R) Double.valueOf(value.toString());
			if (targetType == float.class) return (R) Float.valueOf(value.toString());
			if (targetType == boolean.class) return (R) convertBoolean(value, Boolean.class);
			if (targetType == short.class) return (R) Short.valueOf(value.toString());
			if (targetType == byte.class) return (R) Byte.valueOf(value.toString());
			throw new ClassCastException("Unsupported primitive type: " + targetType.getName());
		}

		private static <R> R convertBoolean(Object value, Class<R> targetType) {
			if (value instanceof Boolean) {
				return targetType.cast(value);
			}
			String strVal = value.toString().toLowerCase();
			boolean boolVal = "true".equals(strVal) || "1".equals(strVal) || "yes".equals(strVal);
			return targetType.cast(boolVal);
		}
		// endregion

		/**
		 * 带默认值的类型转换
		 */
		public static <T, R> R cast(T instance, Class<R> clazz, R defaultValue) {
			try {
				return cast(instance, clazz);
			} catch (Exception e) {
				return defaultValue;
			}
		}

		/**
		 * 集合类型转换
		 */
		public static <T, R> List<R> castList(Collection<T> collection, Class<R> targetType) {
			return collection.stream()
					.map(item -> cast(item, targetType))
					.collect(Collectors.toList());
		}

		/**
		 * 数组类型转换
		 */
		public static <T, R> R[] castArray(T[] array, Class<R> targetType) {
			@SuppressWarnings("unchecked")
			R[] result = (R[]) Array.newInstance(targetType, array.length);
			for (int i = 0; i < array.length; i++) {
				result[i] = cast(array[i], targetType);
			}
			return result;
		}

		public static <T> T cast(Map<String, Object> objMap, Class<T> clazz, T defaultValue, Object... newInstanceParams) {
			if (objMap == null || clazz == null) {
				return defaultValue;
			}
			try {
				T instance = RefTools.newInstance(clazz, newInstanceParams);
				for (Map.Entry<String, Object> entry : objMap.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					Field field = RefTools.getField(clazz, key);
					if (field != null) {
						RefTools.setFieldValue(instance, field, value);
					}
				}

				return instance;
			} catch (Exception e) {
				return defaultValue;
			}
		}

		public static <T> List<T> cast(Collection<Map<String, Object>> collection, Class<T> clazz, T defaultValue, boolean filterNull, Object... newInstanceParams) {
			return collection.stream()
					.map(item -> cast(item, clazz, defaultValue, newInstanceParams))
					.filter(o -> !filterNull || o != null)
					.collect(Collectors.toList());
		}

		public static Class<?> getGenericType(Method method) {
			Type type = method.getGenericReturnType();
			if (type instanceof ParameterizedType parameterizedType) {
				return (Class<?>) parameterizedType.getActualTypeArguments()[0];
			}
			return null;
		}
	}

	public static class AnnoTools {
		public static <T extends Annotation> T getAnnoOnMethod(Method method, Class<T> annotationClass) {
			T annotation = method.getAnnotation(annotationClass);
			if (annotation != null) {
				return annotation;
			}
			for (Annotation anno : method.getAnnotations()) {
				T metaAnnotation = anno.annotationType().getAnnotation(annotationClass);
				if (metaAnnotation != null) {
					return buildSynthesizedAnnotationImpl(anno, metaAnnotation);
				}
			}
			return null;
		}

		public static <T extends Annotation> T getAnnoOnClass(Class<?> clazz, Class<T> annotationClass) {
			T result = clazz.getAnnotation(annotationClass);
			if (result != null) {
				return result;
			}
			for (Annotation anno : clazz.getAnnotations()) {
				T metaAnnotation = anno.annotationType().getAnnotation(annotationClass);
				if (metaAnnotation != null) {
					return buildSynthesizedAnnotationImpl(anno, metaAnnotation);
				}
			}
			return null;
		}

		public static <T extends Annotation> T getAnnoOnField(Field field, Class<T> annotationClass) {
			T result = field.getAnnotation(annotationClass);
			if (result != null) {
				return result;
			}
			for (Annotation anno : field.getAnnotations()) {
				T metaAnnotation = anno.annotationType().getAnnotation(annotationClass);
				if (metaAnnotation != null) {
					return buildSynthesizedAnnotationImpl(anno, metaAnnotation);
				}
			}
			return null;
		}

		public static <T extends Annotation> T getAnnoOnParameter(Method method, int index,Class<T> annotationClass) {
			for (Annotation anno : method.getParameterAnnotations()[index]) {
				T metaAnnotation = anno.annotationType().getAnnotation(annotationClass);
				if (metaAnnotation != null) {
					return buildSynthesizedAnnotationImpl(anno, metaAnnotation);
				}
			}
			return null;
		}


		public static <T> T getValue(Annotation annotation, String attrName, Class<T> type, T defaultValue) {
			try {
				var method = annotation.annotationType().getMethod(attrName);
				Object value = method.invoke(annotation);
				return type.cast(value);
			} catch (Exception e) {
				return defaultValue;
			}
		}

		@SuppressWarnings("unchecked")
		private static <T extends Annotation> T buildSynthesizedAnnotationImpl(Annotation childAnnotation, T metaAnnotation) {
			return (T) Proxy.newProxyInstance(
					childAnnotation.annotationType().getClassLoader(),
					new Class<?>[]{metaAnnotation.annotationType()},
					new AnnotationInvocationHandler<>(childAnnotation, metaAnnotation));
		}

		// 辅助处理类（推荐封装为内部类）
		private record AnnotationInvocationHandler<A extends Annotation, M extends Annotation>(A childAnnotation,
																							   M metaAnnotation) implements InvocationHandler {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				// 排除默认方法和Object方法
				if (method.getDeclaringClass() == Object.class) {
					return method.invoke(this, args);
				}

				try {
					// 1. 优先获取子注解的属性值
					Method childMethod = childAnnotation.annotationType()
							.getMethod(method.getName());
					Object value = childMethod.invoke(childAnnotation);
					if (value != null) {
						return value;
					}
				} catch (Exception ignored) {
					// 若子注解无该属性则忽略
				}

				// 2. 尝试获取元注解的默认值
				try {
					Method metaMethod = metaAnnotation.annotationType()
							.getMethod(method.getName());
					return metaMethod.invoke(metaAnnotation);
				} catch (Exception e) {
					throw new RuntimeException("元注解属性获取失败", e);
				}
			}
		}

	}

	/**
	 * 关闭工具类
	 */
	public static class Close {
		public static void close(Closeable... closeables) {
			close(e -> {
			}, closeables);
		}

		public static void close(AutoCloseable... autoCloseables) {
			close(e -> {
			}, autoCloseables);
		}

		public static void close(Consumer<Exception> exceptionHandler, Closeable... closeables) {
			Arrays.stream(closeables)
					.filter(Objects::nonNull)
					.forEach(r -> {
						try {
							r.close();
						} catch (IOException e) {
							exceptionHandler.accept(e);
						}
					});
		}

		public static void close(Consumer<Exception> exceptionHandler, AutoCloseable... closeables) {
			Arrays.stream(closeables)
					.filter(Objects::nonNull)
					.forEach(r -> {
						try {
							r.close();
						} catch (Exception e) {
							exceptionHandler.accept(e);
						}
					});
		}

	}

	/**
	 * 空值包装类
	 *
	 * @param clazz null的类型
	 * @param <T>   泛型
	 */
	public record NULL<T>(Class<T> clazz) {
	}

	public static <T> NULL<T> NULL(Class<T> clazz) {
		return new NULL<>(clazz);
	}


}
