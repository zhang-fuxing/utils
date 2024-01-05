package com.zhangfuxing.tools.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class NumberUtil {

    public static Double valueOf(Double t) {
        return t == null ? 0.0 : t;
    }

    public static Integer valueOf(Integer num) {
        return num == null ? 0 : num;
    }

    public static <T> Integer toInt(T num) {
        if (num == null)
            return 0;
        int res;
        try {
            res = Integer.parseInt(num.toString());
        } catch (NumberFormatException e) {
            res = 0;
        }
        return res;
    }


    /**
     * 将对象转换到Number对象，主要用于包装类型之间的转换，target是转换目标的类名， source是被转换的源对象
     *
     * @param target 转换目标
     * @param source 被对象
     * @param <T>    泛型T
     * @return 如果能进行转换，则返回目标对象类型，否则返回源类型source
     * @throws Exception 类型转换错误时
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(String target, Object source) throws Exception {
        if (source instanceof BigDecimal b) {
            return convert(target, b);
        }
        if (target.equals("String")) {
            return (T) String.valueOf(source);
        }
        Number cast;
        if (source instanceof Number t) {
            cast = t;
        } else {
            return (T) source;
        }


        String sourceName = cast.getClass().getSimpleName();
        String value = cast.toString();
        if (sourceName.equals(target)) {
            return (T) source;
        }


        boolean isInteget = "Integer".equals(target);
        boolean isLong = "Long".equals(target);
        boolean isFloat = "Float".equals(target);
        boolean isDouble = "Double".equals(target);


        boolean isInteget1 = "Integer".equals(sourceName);
        boolean isLong1 = "Long".equals(sourceName);
        boolean isFloat1 = "Float".equals(sourceName);
        boolean isDouble1 = "Double".equals(sourceName);
        boolean isShort1 = "Short".equals(sourceName);

        T result = null;
        BigDecimal decimal = new BigDecimal(value);


        if (isInteget && (isShort1 || isLong1)) {
            result = (T) (Integer) decimal.intValue();
        } else if (isLong && isInteget1) {
            result = (T) (Long) decimal.longValue();
        } else if (isFloat && isDouble1) {
            result = (T) (Float) decimal.floatValue();
        } else if (isDouble && (isFloat1 || isLong1)) {
            BigDecimal scale = decimal.setScale(2, RoundingMode.HALF_UP);
            result = (T) (Double) scale.doubleValue();
        }


        return result;
    }

    /**
     * 将对象转换到Number对象，主要用于包装类型之间的转换，target是转换目标， source是被转换的源对象
     *
     * @param target 转换目标
     * @param source 被对象
     * @param <T>    泛型T
     * @return 如果能进行转换，则返回目标对象类型，否则返回源类型source
     * @throws Exception 类型转换错误时
     */
    public static <T> T convert(Class<? extends Number> target, Object source) throws Exception {
        if (source instanceof BigDecimal ts) {
            return convert(target, ts);
        }
        return convert(target.getSimpleName(), source);
    }

    public static <T> T convert(Class<? extends Number> target, BigDecimal source) {
        String simpleName = target.getSimpleName();
        return convert(simpleName, source);
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(String target, BigDecimal source) {
        T t;
        try {
            switch (target) {
                case "Integer", "int" -> t = (T) ((Integer) source.intValue());
                case "Long", "long" -> t = (T) ((Long) source.longValue());
                case "Float", "float" -> t = (T) ((Float) source.floatValue());
                case "Double", "double" -> t = (T) ((Double) source.doubleValue());
                case "Short", "short" -> t = (T) ((Short) source.shortValue());
                default -> t = (T) source;
            }
        } catch (Exception e) {
            t = (T) source;
        }
        return t;
    }

    private static final Map<String, Number> initMap = new HashMap<>(6);

    static {
        initMap.put("Integer", 0);
        initMap.put("Byte", (byte) 0);
        initMap.put("Short", (short) 0);
        initMap.put("Long", 0L);
        initMap.put("Float", 0.0F);
        initMap.put("Double", 0.0);
    }

    public static <T extends Number> T getInstance(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        return (T) initMap.get(type.getSimpleName());
    }

    /**
     * 传入的数字大于0
     *
     * @param number num
     * @param <T>    t
     * @return bool
     */
    public static <T extends Number> boolean isPositive(T number) {
        if (number == null) return false;
        String s = number.toString();
        double num;
        try {
            num = java.lang.Double.parseDouble(s);
        } catch (NumberFormatException e) {
            num = 0;
        }
        return num > 0.0;
    }

    public static Float subNum(Float f, int num) {
        if (f == null) return 0f;
        BigDecimal bigDecimal = new BigDecimal(f.toString());
        return bigDecimal.setScale(num, RoundingMode.HALF_UP).floatValue();
    }

    public static Double subNum(Double f, int num) {
        if (f == null) return (double) 0;
        BigDecimal bigDecimal = new BigDecimal(f.toString());
        return bigDecimal.setScale(num, RoundingMode.HALF_UP).doubleValue();
    }


    public static synchronized void set(Double target, Double value) {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Unsafe unsafe = (Unsafe) field.get(null);
            long valueOffset = unsafe.objectFieldOffset(Double.class.getDeclaredField("value"));
            unsafe.putDouble(target, valueOffset, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Double scale(Double value, int c) {
        if (value == null) return null;
        return new BigDecimal(value).setScale(c, RoundingMode.HALF_UP).doubleValue();
    }
}
