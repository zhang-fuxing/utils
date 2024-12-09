package com.zhangfuxing.tools.db.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据库结果集解析
 *
 * @author 张福兴
 * @version 1.0
 * @date 2024/12/6
 * @email zhangfuxing1010@163.com
 */
@SuppressWarnings("unchecked")
public abstract class DbSetResolve {
    private static final Logger log = LoggerFactory.getLogger(DbSetResolve.class);

    private final Map<Class<?>, MemoryCache> cache = new ConcurrentHashMap<>();

    /**
     * 是否使用数据库查询到的列的索引进行数据转换
     */
    protected boolean useDbIndexConvert;

    /**
     * 获取当前字段对应的数据库（SQL结果）列名
     *
     * @param field 当前字段
     * @return 映射名称
     */
    protected abstract String getColumnName(Field field);

    /**
     * 获取实体中是SQL列的字段集合
     *
     * @param entityClass 实体类型
     * @return 对应的实体字段列表
     */
    protected abstract List<Field> getEntityColumnField(Class<?> entityClass);

    /**
     * 提取 ResultSet 的值，并提供 Field 设置到 obj 对象中
     *
     * @param rs    SQL 结果集
     * @param obj   实体实例
     * @param field 实体字段
     * @param <T>   泛型
     */
    protected <T> void extractResultSetValue(ResultSet rs, T obj, Field field) {
        String columnName = getColumnName(field);
        if (columnName == null) {
            return;
        }
        Class<?> clazz = field.getType();
        try {
            field.setAccessible(true);
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

    /**
     * 提取 ResultSet 的值，并提供 Field 设置到 obj 对象中
     *
     * @param rs    SQL 结果集
     * @param index 结果集的索引位置
     * @param obj   实体实例
     * @param field 实体字段
     * @param <T>   泛型
     */
    protected <T> void extractResultSetValue(ResultSet rs, int index, T obj, Field field) {
        String simpleName = field.getType().getSimpleName();
        try {
            field.setAccessible(true);
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


    // -------------------------------- public -------------------------------- //

    /**
     * 根据结果集获取MAP行集合
     *
     * @param resultSet 数据库查询的结果集
     * @return 所有的行Map集合
     */
    public List<Map<String, Object>> getMap(ResultSet resultSet) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
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

    /**
     * 提供结果集获取统计值
     *
     * @param rs 数据库查询的结果集
     * @return 统计值
     */
    public long countValue(ResultSet rs) {
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

    /**
     * 通过结果集获取实体类的列表集合
     *
     * @param rs    数据库查询的结果集
     * @param clazz 实体类型
     * @param <T>   泛型T
     * @return 实体实例集合
     */
    public <T> List<T> getEntities(ResultSet rs, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        while (true) {
            T obj = next(rs, clazz);
            if (obj == null) {
                break;
            }
            result.add(obj);
        }
        return result;
    }

    /**
     * 获取结果集的下一条行数据，并转换为MAP集合
     *
     * @param rs 数据库查询的结果集
     * @return 行数据集合
     */
    public Map<String, Object> next(ResultSet rs) {
        if (rs == null) {
            return null;
        }
        try {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            if (!rs.next()) {
                return null;
            }
            Map<String, Object> result = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = md.getColumnName(i);
                Object object = rs.getObject(i);
                result.put(columnName, object);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取结果集的下一条行数据，并转换为实体类
     *
     * @param rs    数据库查询的结果集
     * @param clazz 实体类型
     * @param <T>   泛型
     * @return 实体实例，如果结果为空，返回null
     */
    public <T> T next(ResultSet rs, Class<T> clazz) {
        if (rs == null) {
            return null;
        }
        if (clazz == null) {
            throw new NullPointerException("请指定要转换的类型");
        }
        try {
            EntityMetaData<T> metaData = getEntityMetaData(clazz);
            T obj = null;
            if (rs.next()) {
                obj = metaData.constructor().newInstance();
                if (!useDbIndexConvert) {
                    for (Field field : metaData.fieldList()) {
                        extractResultSetValue(rs, obj, field);
                    }
                } else {
                    Map<String, Field> fieldMap = metaData.fieldList().stream().collect(Collectors.toMap(f -> f.getName().toUpperCase(), f -> f));
                    ResultSetMetaData md = rs.getMetaData();
                    int columnCount = md.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = md.getColumnName(i);
                        Field field = fieldMap.get(columnName.toUpperCase());
                        if (field == null) {
                            continue;
                        }
                        extractResultSetValue(rs, i, obj, field);
                    }
                }
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取实体类的元信息，包括构造函数和字段列表
     *
     * @param clazz 实体类
     * @param <T>   泛型
     * @return 元信息对象
     */
    private <T> EntityMetaData<T> getEntityMetaData(Class<T> clazz) {
        Constructor<T> constructor;
        List<Field> fieldList;
        MemoryCache memoryCache = cache.get(clazz);
        if (memoryCache == null) {
            try {
                constructor = clazz.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("类 %s 没有无参构造函数,请提供无参构造。".formatted(clazz.getName()));
            }
            fieldList = getEntityColumnField(clazz);
            memoryCache = new MemoryCache();
            memoryCache.constructor = constructor;
            memoryCache.entityClass = clazz;
            memoryCache.fieldList = fieldList;
            cache.put(clazz, memoryCache);
        } else {
            constructor = (Constructor<T>) memoryCache.constructor;
            fieldList = getEntityColumnField(clazz);
        }
        return new EntityMetaData<>(constructor, fieldList);
    }


    // -------------------------------- public -------------------------------- //

    LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        ZonedDateTime zonedDateTime = getZonedDateTime(date, ZoneId.systemDefault());
        return zonedDateTime.toLocalDateTime();
    }

    LocalDate toLocalDate(Date date) {
        return toLocalDate(date, ZoneId.systemDefault());
    }

    LocalTime toLocalTime(Date date) {
        if (date == null) return null;
        ZonedDateTime zonedDateTime = getZonedDateTime(date, ZoneId.systemDefault());
        return zonedDateTime.toLocalTime();
    }

    LocalDate toLocalDate(Date date, ZoneId zoneId) {
        if (date == null) return null;
        ZonedDateTime zonedDateTime = getZonedDateTime(date, zoneId);
        return zonedDateTime.toLocalDate();
    }

    ZonedDateTime getZonedDateTime(Date date, ZoneId zoneId) {
        Instant instant = date.toInstant();
        zoneId = Objects.requireNonNullElse(zoneId, ZoneId.systemDefault());
        return instant.atZone(zoneId);
    }

    public void setUseDbIndexConvert(boolean useDbIndexConvert) {
        this.useDbIndexConvert = useDbIndexConvert;
    }

    private static class MemoryCache {
        /**
         * 实体类型
         */
        Class<?> entityClass;

        /**
         * 无参构造
         */
        Constructor<?> constructor;

        /**
         * 实体字段
         */
        List<Field> fieldList;

        public <T> T newInstance() {
            if (constructor == null) {
                try {
                    constructor = entityClass.getConstructor();
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("请为 %s 提供无参构造".formatted(entityClass.getName()), e);
                }
            }
            try {
                return (T) constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("无法创建 %s 的实例对象".formatted(entityClass.getName()), e);
            }
        }

    }

    private record EntityMetaData<T>(Constructor<T> constructor, List<Field> fieldList) {
    }
}
