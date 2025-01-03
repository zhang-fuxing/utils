package com.zhangfuxing.tools.db.convert;

/**
 * 自定义类型转换器接口
 */
public interface TypeConverter<S, T> {
    /**
     * 判断是否可以处理该类型转换
     */
    boolean canConvert(Class<?> sourceType, Class<?> targetType);
    
    /**
     * 执行类型转换
     */
    T convert(S source, Class<T> targetType);
} 