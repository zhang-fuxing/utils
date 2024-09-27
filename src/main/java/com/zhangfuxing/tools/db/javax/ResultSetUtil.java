package com.zhangfuxing.tools.db.javax;


import javax.persistence.Column;
import javax.persistence.Id;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.*;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/27
 * @email zhangfuxing1010@163.com
 */
public class ResultSetUtil {

    public static void iterate(ResultSet resultSet, BiConsumer<ResultSetMetaData, Integer> consumer) {
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    if (consumer != null) {
                        consumer.accept(metaData, i);
                    } else {
                        String columnName = metaData.getColumnName(i);
                        Object object = resultSet.getObject(columnName);
                        System.out.printf("Column %s: %s\n", columnName, object);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void iterateFirst(ResultSet resultSet, BiConsumer<ResultSetMetaData, Integer> consumer) {
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            if (resultSet.next() && columnCount > 0) {
                for (int i = 1; i <= columnCount; i++) {
                    if (consumer != null) {
                        consumer.accept(metaData, i);
                    } else {
                        String columnTypeName = metaData.getColumnTypeName(i);
                        String columnClassName = metaData.getColumnClassName(i);
                        String columnName = metaData.getColumnName(i);
                        Object object = resultSet.getObject(columnName);
                        System.out.printf("Column %s: %s; Type: %s,Class: %s\n", columnName, object, columnTypeName, columnClassName);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static long count(ResultSet rs) throws SQLException {
        try {
            boolean next = rs.next();
            if (next) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0L;
    }

    public static <T> List<T> toList(ResultSet rs, Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        if (rs == null) {
            return new ArrayList<T>();
        }
        if (clazz == null) {
            throw new NullPointerException("请指定要转换的类型");
        }
        Constructor<T> constructor;
        try {
            constructor = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("类 %s 没有无参构造函数".formatted(clazz.getName()));
        }

        List<Field> fieldList = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> {
                    field.setAccessible(true);
                    boolean javaxColumn = field.isAnnotationPresent(Column.class);
                    boolean javaxId = field.isAnnotationPresent(Id.class);
                    return javaxColumn || javaxId;
                })
                .toList();
        if (fieldList.isEmpty()) {
            return list;
        }

        try {
            while (rs.next()) {
                T obj = constructor.newInstance();
                for (Field field : fieldList) {
                    set(rs, obj, field);
                }
                list.add(obj);
            }
        } catch (SQLException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    private static <T> void set(ResultSet rs, T obj, Field field) {
        String columnName = getColumnName(field);
        if (columnName == null) {
            return;
        }
        Class<?> clazz = field.getType();
        try {
            switch (clazz.getSimpleName().toLowerCase()) {
                case "int", "integer" -> field.set(obj, rs.getInt(columnName));
                case "long" -> field.set(obj, rs.getLong(columnName));
                case "float" -> field.set(obj, rs.getFloat(columnName));
                case "double" -> field.set(obj, rs.getDouble(columnName));
                case "boolean" -> field.set(obj, rs.getBoolean(columnName));
                case "byte" -> field.set(obj, rs.getByte(columnName));
                case "short" -> field.set(obj, rs.getShort(columnName));
                case "char", "character", "string" -> field.set(obj, rs.getString(columnName));
                case "bigdecimal" -> field.set(obj, rs.getBigDecimal(columnName));
                case "byte[]" -> field.set(obj, rs.getBytes(columnName));
                // 日期数据转换
                case "date", "time", "timestamp" -> field.set(obj, rs.getTimestamp(columnName));
                case "localdate" -> field.set(obj, toLocalDate(rs.getTimestamp(columnName)));
                case "localtime" -> field.set(obj, toLocalTime(rs.getTimestamp(columnName)));
                case "localdatetime" -> field.set(obj, toLocalDateTime(rs.getTimestamp(columnName)));

                case "inputstream" -> field.set(obj, rs.getBinaryStream(columnName));
                case "ref" -> field.set(obj, rs.getRef(columnName));
                case "blob" -> field.set(obj, rs.getBlob(columnName));
                case "clob" -> field.set(obj, rs.getClob(columnName));
                case "array" -> field.set(obj, rs.getArray(columnName));
                case "url" -> field.set(obj, rs.getURL(columnName));
                case "rowid" -> field.set(obj, rs.getRowId(columnName));
                case "nclob" -> field.set(obj, rs.getNClob(columnName));
                case "sqlxml" -> field.set(obj, rs.getSQLXML(columnName));
                default -> {
                }
            }
        } catch (IllegalAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        ZonedDateTime zonedDateTime = getZonedDateTime(date, ZoneId.systemDefault());
        return zonedDateTime.toLocalDateTime();
    }

    private static LocalDate toLocalDate(Date date) {
        return toLocalDate(date, ZoneId.systemDefault());
    }

    private static LocalTime toLocalTime(Date date) {
        if (date == null) return null;
        ZonedDateTime zonedDateTime = getZonedDateTime(date, ZoneId.systemDefault());
        return zonedDateTime.toLocalTime();
    }

    private static LocalDate toLocalDate(Date date, ZoneId zoneId) {
        if (date == null) return null;
        ZonedDateTime zonedDateTime = getZonedDateTime(date, zoneId);
        return zonedDateTime.toLocalDate();
    }

    private static ZonedDateTime getZonedDateTime(Date date, ZoneId zoneId) {
        Instant instant = date.toInstant();
        zoneId = Objects.requireNonNullElse(zoneId, ZoneId.systemDefault());
        return instant.atZone(zoneId);
    }

    private static String getColumnName(Field field) {
        String result = null;
        String name = field.getName();
        var column1 = field.getAnnotation(Column.class);
        var id1 = field.getAnnotation(Id.class);
        if (column1 != null) {
            result = column1.name();
            if (result.isEmpty()) {
                result = name;
            }
        }
        if (id1 != null) {
            result = name;
        }
        return result;
    }
}
