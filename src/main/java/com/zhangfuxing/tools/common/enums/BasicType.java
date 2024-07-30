package com.zhangfuxing.tools.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/7/12
 * @email zhangfuxing1010@163.com
 */
public enum BasicType {
    BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, CHAR, BOOLEAN;
    private static final Map<Class<?>, Class<?>> typeMapping;
    private static final Map<Class<?>, Class<?>> typeUnMapping;

    static {
        typeMapping = new HashMap<>(8);
        typeMapping.put(boolean.class, Boolean.class);
        typeMapping.put(byte.class, Byte.class);
        typeMapping.put(char.class, Character.class);
        typeMapping.put(double.class, Double.class);
        typeMapping.put(float.class, Float.class);
        typeMapping.put(int.class, Integer.class);
        typeMapping.put(long.class, Long.class);
        typeMapping.put(short.class, Short.class);

        typeUnMapping = new HashMap<>(8);
        typeUnMapping.put(Boolean.class, boolean.class);
        typeUnMapping.put(Byte.class, byte.class);
        typeUnMapping.put(Character.class, char.class);
        typeUnMapping.put(Double.class, double.class);
        typeUnMapping.put(Float.class, float.class);
        typeUnMapping.put(Integer.class, int.class);
        typeUnMapping.put(Long.class, long.class);
        typeUnMapping.put(Short.class, short.class);
    }

    public static Class<?> wrapper(Class<?> clazz) {
        if (clazz == null || !clazz.isPrimitive()) {
            return clazz;
        }
        Class<?> result = typeMapping.get(clazz);
        return (null == result) ? clazz : result;
    }

    public static Class<?> unwrapper(Class<?> clazz){
        if(null == clazz || clazz.isPrimitive()){
            return clazz;
        }
        Class<?> result = typeUnMapping.get(clazz);
        return (null == result) ? clazz : result;
    }

    public static boolean isBasicType(Class<?> clazz) {
        if (clazz == null) return false;
        return typeMapping.containsKey(clazz) || typeUnMapping.containsKey(clazz);
    }
}
