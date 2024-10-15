package com.zhangfuxing.tools.db.jakarta;


import jakarta.persistence.Column;
import jakarta.persistence.Id;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/27
 * @email zhangfuxing1010@163.com
 */
@SuppressWarnings("DuplicatedCode")
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

    public static <T> List<T> toList(ResultSet rs, Class<T> clazz, boolean useIndex) {
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
                    boolean javaxColumn = field.isAnnotationPresent(javax.persistence.Column.class);
                    boolean javaxId = field.isAnnotationPresent(javax.persistence.Id.class);
                    return javaxColumn || javaxId;
                })
                .toList();
        if (fieldList.isEmpty()) {
            return list;
        }
        Map<String, Field> fieldMap = fieldList.stream().collect(Collectors.toMap(Field::getName, f -> f));
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                T obj = constructor.newInstance();
                if (!useIndex) {
                    for (Field field : fieldList) {
                        set(rs, obj, field);
                    }
                } else {
                    int columnCount = metaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Field field = fieldMap.get(columnName);
                        if (field == null) {
                            continue;
                        }
                        set(rs, i, obj, field);
                    }
                }

                list.add(obj);
            }
        } catch (SQLException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    private static <T> void set(ResultSet rs, int index, T obj, Field field) {
        String simpleName = field.getType().getSimpleName();
        try {
            switch (simpleName.toLowerCase()) {
                case "int", "integer" -> field.set(obj, rs.getInt(index));
                case "long" -> field.set(obj, rs.getLong(index));
                case "float" -> field.set(obj, rs.getFloat(index));
                case "double" -> field.set(obj, rs.getDouble(index));
                case "boolean" -> field.set(obj, rs.getBoolean(index));
                case "byte" -> field.set(obj, rs.getByte(index));
                case "short" -> field.set(obj, rs.getShort(index));
                case "char", "character", "string" -> field.set(obj, rs.getString(index));
                case "bigdecimal" -> field.set(obj, rs.getBigDecimal(index));
                case "byte[]" -> field.set(obj, rs.getBytes(index));
                // 日期数据转换
                case "date", "time", "timestamp" -> field.set(obj, rs.getTimestamp(index));
                case "localdate" -> field.set(obj, toLocalDate(rs.getTimestamp(index)));
                case "localtime" -> field.set(obj, toLocalTime(rs.getTimestamp(index)));
                case "localdatetime" -> field.set(obj, toLocalDateTime(rs.getTimestamp(index)));
                case "inputstream" -> field.set(obj, rs.getBinaryStream(index));
                case "ref" -> field.set(obj, rs.getRef(index));
                case "blob" -> field.set(obj, rs.getBlob(index));
                case "clob" -> field.set(obj, rs.getClob(index));
                case "array" -> field.set(obj, rs.getArray(index));
                case "url" -> field.set(obj, rs.getURL(index));
                case "rowid" -> field.set(obj, rs.getRowId(index));
                case "nclob" -> field.set(obj, rs.getNClob(index));
                case "sqlxml" -> field.set(obj, rs.getSQLXML(index));
                default -> {
                }
            }
        } catch (IllegalAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
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
        var column0 = field.getAnnotation(Column.class);
        var id0 = field.getAnnotation(Id.class);
        if (column0 != null) {
            result = column0.name();
            if (result.isEmpty()) {
                result = name;
            }
        }
        if (id0 != null) {
            result = name;
        }
        return result;
    }

    public static List<Map<String, Object>> toMap(ResultSet resultSet) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    map.put(columnName, value);
                }
                result.add(map);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
