package org.tools.option;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class DateUtil {
	/**
	 * 时间戳转换成日期格式字符串
	 *
	 * @param seconds 精确到秒的字符串
	 */
	public static String timeStamp2Date(String seconds, String format) {
		if (null == seconds || seconds.isEmpty() || seconds.equals("null")) {
			return "";
		}
		if (null == format || format.isEmpty()) {
			format = "yyyy-MM-dd HH:mm:ss";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date(Long.parseLong(seconds + "000")));
	}
	
	/**
	 * 日期格式字符串转换成时间戳
	 *
	 * @param dateStr 字符串日期
	 */
	public static String date2TimeStamp(String dateStr, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return String.valueOf(sdf.parse(dateStr).getTime() / 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 取得当前时间戳（精确到秒）
	 */
	public static String timeStamp() {
		long time = System.currentTimeMillis();
		return String.valueOf(time / 1000);
	}
	
	/**
	 * @param beginDate 起始日期
	 * @param limitDay  时限天数
	 */
	public static Date getLimitDate(Date beginDate, int limitDay) {
		if (null == beginDate) {
			return null;
		}
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(beginDate);
		calendar.add(Calendar.DATE, limitDay);
		return calendar.getTime();
	}
	
	public static String format(Date date) {
		return format(date, "yyyy-MM-dd HH:mm:ss SSS");
	}
	
	public static String format(Date date, String pattern) {
		if (date == null)
			return "";
		SimpleDateFormat sdf;
		if (Str_Util.isEmpty(pattern))
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		else
			sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}
	
	public static LocalDate parse(String str, String pattern) {
		return LocalDate.parse(str, DateTimeFormatter.ofPattern(pattern));
	}
	
	public static LocalDateTime parseToLocalDateTime(String str, String pattern) {
		return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(pattern));
	}
	
	public static LocalDateTime parseToLocalDateTime(String str) {
		return parseToLocalDateTime(str, "yyyy-MM-dd hh:mm:ss SSS");
	}
	
	
	public static LocalDate parse(String str) {
		LocalDate date = null;
		List<String> list = List.of("yyyyMMdd", "yyyy/MM/dd", "yyyy-MM-dd", "yyyy MM dd", "yyyy.MM.dd", "yyyy年MM月dd日");
		for (String pattern : list) {
			try {
				date = parse(str, pattern);
				break;
			} catch (Exception ignored) {
			}
		}
		return date;
	}
	
	public static Date localDateTimeToDate(LocalDateTime localDateTime) {
		Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}
	
	public static Date localDateToDate(LocalDate localDate) {
		Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}
	
	public static Date localTimeToDate(LocalTime localTime) {
		LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), localTime);
		return localDateTimeToDate(localDateTime);
	}
	
	public static LocalDateTime dateToLocalDateTime(Date date) {
		return getZoneDateTime(date).toLocalDateTime();
	}
	
	public static LocalDate dateToLocalDate(Date date) {
		return getZoneDateTime(date).toLocalDate();
	}
	
	public static LocalTime dateToLocalTime(Date date) {
		return getZoneDateTime(date).toLocalTime();
	}
	
	private static ZonedDateTime getZoneDateTime(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault());
	}
	
	public static Date getDateOffset(Date date, long offset, TemporalUnit unit) {
		LocalDateTime localDateTime = dateToLocalDateTime(date);
		localDateTime = localDateTime.plus(offset, unit);
		return localDateTimeToDate(localDateTime);
	}
	
	/**
	 * 获取偏移后的日期，默认时间单位是分钟
	 *
	 * @param date   从哪个时间点偏移，例如现在
	 * @param offset 偏移多少
	 * @return 偏移后的时间
	 */
	public static Date getDateOffset(Date date, long offset) {
		return getDateOffset(date, offset, ChronoUnit.MINUTES);
	}
	
	/**
	 * 偏移3600分钟后的时间
	 *
	 * @param date 偏移时间点
	 * @return 偏移后的时间
	 */
	public static Date getDateOffset(Date date) {
		return getDateOffset(date, 3600);
	}
}
