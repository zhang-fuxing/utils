package com.zhangfuxing.tools.date;

import com.zhangfuxing.tools.util.Str;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/27
 * @email zhangfuxing1010@163.com
 */
public class Date7Util {
    public static String[] patterns = {
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy/MM/dd HH:mm:ss.SSS",
            "yyyy年MM月dd日 HH时mm分ss.SSS秒",
            "yyyy-MM-dd HH:mm:ss SSS",
            "yyyy/MM/dd HH:mm:ss SSS",
            "yyyy年MM月dd日 HH时mm分ss SSS秒",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy年MM月dd日 HH时mm分ss秒",
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "yyyy年MM月dd日"

    };
    public static Date parseDate(String date) {
        return parseDate(date, null);
    }

    public static Date parseDate(String dateStr, String pattern) {
        if (Str.isBlank(dateStr)) return null;
        dateStr = dateStr.trim();
        try {
            return parseDate0(dateStr, pattern);
        } catch (Exception e) {
            for (String p : patterns) {
                try {
                    Date date = parseDate0(dateStr, p);
                    if (date != null) return date;
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }


    private static Date parseDate0(String dateStr, String pattern) {
        if (Str.isBlank(dateStr)) return null;
        dateStr = dateStr.trim();
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.parse(dateStr, new ParsePosition(0));
    }

    public static Date toDate(LocalDate localDate, ZoneId zoneId) {
        if (localDate == null) return null;
        ZoneId zone = getZoneId(zoneId);
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(zone);
        return Date.from(zonedDateTime.toInstant());
    }

    public static Date toDate(LocalDate localDate) {
        return toDate(localDate, ZoneId.systemDefault());
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return toDate(localDateTime, ZoneId.systemDefault());
    }

    public static Date toDate(LocalDateTime localDateTime, ZoneId zoneId) {
        if (localDateTime == null) return null;
        ZoneId zone = getZoneId(zoneId);
        ZonedDateTime zonedDateTime = localDateTime.atZone(zone);
        return Date.from(zonedDateTime.toInstant());
    }

    private static ZoneId getZoneId(ZoneId zoneId) {
        return Objects.requireNonNullElse(zoneId, ZoneId.systemDefault());
    }
}
