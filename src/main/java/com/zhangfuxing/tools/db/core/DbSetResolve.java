package com.zhangfuxing.tools.db.core;

import com.zhangfuxing.tools.db.convert.TypeConverterRegistry;
import com.zhangfuxing.tools.db.handler.FieldValueHandler;
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

    private static final Map<Class<?>, MemoryCache> cache = new ConcurrentHashMap<>();

    private final List<FieldValueHandler> fieldHandlers = new ArrayList<>();
    private boolean useTypeConverter = true;

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
        log.debug("Mapping field: {} to column: {}", field.getName(), columnName);
        if (columnName == null) {
            return;
        }
        Class<?> clazz = field.getType();
        try {
            field.setAccessible(true);
            Object value = rs.getObject(columnName);
            log.debug("Column {} value: {}", columnName, value);
            
            // 应用字段处理器
            for (FieldValueHandler handler : fieldHandlers) {
                if (handler.canHandle(field)) {
                    value = handler.handleValue(value, field);
                }
            }
            
            if (value == null) {
                field.set(obj, null);
                return;
            }

            // 尝试使用类型转换器
            if (useTypeConverter) {
                try {
                    Object converted = TypeConverterRegistry.convert(value, field.getType());
                    field.set(obj, converted);
                    return;
                } catch (IllegalArgumentException e) {
                    // 转换失败，继续使用默认转换逻辑
                }
            }

            if (value.getClass().equals(clazz) || clazz.isAssignableFrom(value.getClass())) {
                field.set(obj, value);
                return;
            }

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
                case "date", "time", "timestamp" -> {
                    java.sql.Timestamp timestamp = rs.getTimestamp(columnName);
                    if (clazz == java.sql.Date.class) {
                        field.set(obj, timestamp != null ? new java.sql.Date(timestamp.getTime()) : null);
                    } else if (clazz == java.sql.Time.class) {
                        field.set(obj, timestamp != null ? new java.sql.Time(timestamp.getTime()) : null);
                    } else {
                        field.set(obj, timestamp);
                    }
                }
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
                    if (clazz.isEnum()) {
                        String enumValue = rs.getString(columnName);
                        if (enumValue != null) {
                            try {
                                field.set(obj, Enum.valueOf((Class<? extends Enum>) clazz, enumValue));
                            } catch (IllegalArgumentException e) {
                                log.warn("枚举值 {} 不存在于 {}", enumValue, clazz.getName());
                                field.set(obj, null);
                            }
                        }
                    } else {
                        try {
                            field.set(obj, value);
                        } catch (IllegalArgumentException e) {
                            log.error("字段 {} 类型转换失败: {} -> {}", field.getName(), value.getClass(), clazz);
                            throw e;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("字段 {} 转换失败: {}", field.getName(), e.getMessage());
            throw new RuntimeException(String.format("字段 %s 转换失败: %s", field.getName(), e.getMessage()), e);
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
            // 先检查结果集中的值是否为null
            Object value = rs.getObject(index);
            if (value == null) {
                field.set(obj, null);
                return;
            }

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
        List<Map<String, Object>> result = Collections.synchronizedList(new ArrayList<>());
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> map = Collections.synchronizedMap(new LinkedHashMap<>());
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
        return result.stream()
				.map(LinkedHashMap::new).collect(Collectors.toList());
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
        List<T> result = Collections.synchronizedList(new ArrayList<>());
        while (true) {
            T obj = next(rs, clazz);
            if (obj == null) {
                break;
            }
            result.add(obj);
        }
        return new ArrayList<>(result);
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
            Map<String, Object> result = Collections.synchronizedMap(new LinkedHashMap<>());
            for (int i = 1; i <= columnCount; i++) {
                String columnName = md.getColumnName(i);
                Object object = rs.getObject(i);
                result.put(columnName, object);
            }
            return new LinkedHashMap<>(result);
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
            log.debug("Found {} fields in entity class {}", 
                metaData.fieldList().size(), clazz.getSimpleName());
            
            T obj = null;
            if (rs.next()) {
                obj = metaData.constructor().newInstance();
                if (!useDbIndexConvert) {
                    for (Field field : metaData.fieldList()) {
                        String columnName = getColumnName(field);
                        log.debug("Trying to get value for field {} from column {}", 
                            field.getName(), columnName);
                        try {
                            Object value = rs.getObject(columnName);
                            log.debug("Value found for column {}: {}", columnName, value);
                            extractResultSetValue(rs, obj, field);
                        } catch (SQLException e) {
                            log.error("Error getting value for column {}: {}", 
                                columnName, e.getMessage());
                            throw e;
                        }
                    }
                }
            }
            return obj;
        } catch (Exception e) {
            log.error("实体转换失败: {}", e.getMessage(), e);
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
        MemoryCache memoryCache = cache.get(clazz);
        if (memoryCache == null) {
            synchronized (cache) {
                memoryCache = cache.get(clazz);
                if (memoryCache == null) {
                    try {
                        Constructor<T> constructor = clazz.getDeclaredConstructor();
                        List<Field> fieldList = getEntityColumnField(clazz);
                        memoryCache = new MemoryCache();
                        memoryCache.constructor = constructor;
                        memoryCache.entityClass = clazz;
                        memoryCache.fieldList = fieldList;
                        // 初始化字段映射
                        for (Field field : fieldList) {
                            String columnName = getColumnName(field);
                            memoryCache.addField(field, columnName);
                        }
                        cache.put(clazz, memoryCache);
                    } catch (NoSuchMethodException e) {
                        throw new IllegalArgumentException("类 %s 没有无参构造函数,请提供无参构造。".formatted(clazz.getName()));
                    }
                }
            }
        }
        return new EntityMetaData<>((Constructor<T>)memoryCache.constructor, memoryCache.fieldList);
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
        zoneId = Objects.requireNonNullElse(zoneId, DEFAULT_ZONE_ID);
        return instant.atZone(zoneId);
    }

    public void setUseDbIndexConvert(boolean useDbIndexConvert) {
        this.useDbIndexConvert = useDbIndexConvert;
    }

    public void addFieldHandler(FieldValueHandler handler) {
        fieldHandlers.add(handler);
    }

    public void setUseTypeConverter(boolean useTypeConverter) {
        this.useTypeConverter = useTypeConverter;
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

        /**
         * 字段名到Field的映射缓存
         */
        ConcurrentHashMap<String, Field> fieldNameMapping;

        /**
         * 列名到Field的映射缓存
         */
        ConcurrentHashMap<String, Field> columnNameMapping;

        private final Object lock = new Object();

        public MemoryCache() {
            this.fieldNameMapping = new ConcurrentHashMap<>();
            this.columnNameMapping = new ConcurrentHashMap<>();
        }

        public void addField(Field field, String columnName) {
            String fieldKey = field.getName().toUpperCase();
            String columnKey = columnName != null ? columnName.toUpperCase() : fieldKey;
            
            fieldNameMapping.put(fieldKey, field);
            if (columnName != null) {
                columnNameMapping.put(columnKey, field);
                log.debug("Mapped column {} to field {}", columnKey, field.getName());
            }
        }

        public <T> T newInstance() {
            synchronized (lock) {
                if (constructor == null) {
                    try {
                        constructor = entityClass.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("请为 %s 提供无参构造".formatted(entityClass.getName()), e);
                    }
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

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
}
