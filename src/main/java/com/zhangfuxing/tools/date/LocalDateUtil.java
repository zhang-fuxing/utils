package com.zhangfuxing.tools.date;

import java.time.*;
import java.util.Date;
import java.util.Objects;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/27
 * @email zhangfuxing1010@163.com
 */
public class LocalDateUtil {

    public static LocalDateTime parseLocalDateTime(String str) {
        return parseLocalDateTime(str, null);
    }

    public static LocalDateTime parseLocalDateTime(String dateStr, String pattern) {
        Date date = Date7Util.parseDate(dateStr, pattern);
        return toLocalDateTime(date);
    }



    public static LocalDate toLocalDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.toLocalDate();
    }

    public static LocalDate toLocalDate(Date date) {
        return toLocalDate(date, ZoneId.systemDefault());
    }

    public static LocalDate toLocalDate(Date date, ZoneId zoneId) {
        if (date == null) return null;
        ZonedDateTime zonedDateTime = getZonedDateTime(date, zoneId);
        return zonedDateTime.toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        ZonedDateTime zonedDateTime = getZonedDateTime(date, ZoneId.systemDefault());
        return zonedDateTime.toLocalDateTime();
    }

    public static LocalDateTime toLocalDateTime(Date date, ZoneId zoneId) {
        if (date == null) return null;
        ZonedDateTime zonedDateTime = getZonedDateTime(date, zoneId);
        return zonedDateTime.toLocalDateTime();
    }

    public static LocalDateTime toLocalDateTime(LocalDate date) {
        if (date == null) return null;
        return LocalDateTime.of(date, LocalTime.MIDNIGHT);
    }

    public static LocalTime toLocalTime(Date date) {
        if (date == null) return null;
        ZonedDateTime zonedDateTime = getZonedDateTime(date, ZoneId.systemDefault());
        return zonedDateTime.toLocalTime();
    }

    public static LocalTime toLocalTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.toLocalTime();
    }


    private static ZonedDateTime getZonedDateTime(Date date, ZoneId zoneId) {
        Instant instant = date.toInstant();
        zoneId = Objects.requireNonNullElse(zoneId, ZoneId.systemDefault());
        return instant.atZone(zoneId);
    }
}
