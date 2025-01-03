package com.zhangfuxing.tools.db.convert;

public class StringToEnumConverter implements TypeConverter<String, Enum<?>> {
    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return String.class.equals(sourceType) && targetType.isEnum();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Enum<?> convert(String source, Class targetType) {
        return Enum.valueOf(targetType, source);
    }
} 