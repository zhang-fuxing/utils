package com.zhangfuxing.tools;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2023/05/31
 * @email zhangfuxing1010@163.com
 */
public class UnsafeUtil {

    public static synchronized Unsafe getInstance() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized long getOffset(Field field) {
        return getInstance().objectFieldOffset(field);
    }

    public static synchronized void updateDouble(Double target, Double value) {
        Unsafe unsafe = getInstance();
        long offset;
        try {
            offset = getOffset(Double.class.getDeclaredField("value"));
        } catch (NoSuchFieldException e) {
            return;
        }
        unsafe.putDouble(target, offset, value);
    }

}
