package com.zhangfuxing.tools.db.convert;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TypeConverterRegistry {
    private static final List<TypeConverter<?, ?>> converters = new CopyOnWriteArrayList<>();
    
    public static void registerConverter(TypeConverter<?, ?> converter) {
        converters.add(converter);
    }
    
    @SuppressWarnings("unchecked")
    public static <S, T> T convert(S source, Class<T> targetType) {
        if (source == null) return null;
        
        Class<?> sourceType = source.getClass();
        for (TypeConverter<?, ?> converter : converters) {
            if (converter.canConvert(sourceType, targetType)) {
                return ((TypeConverter<S, T>) converter).convert(source, targetType);
            }
        }
        throw new IllegalArgumentException("No converter found for " + sourceType + " to " + targetType);
    }
} 