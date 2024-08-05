package com.zhangfuxing.tools.util;

import java.lang.reflect.InvocationTargetException;

/**
 * @author zhangfx
 * @date 2023/4/11
 */
public class ClassUtil {
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(T obj) {
        return ((null == obj) ? null : (Class<T>) obj.getClass());
    }

    public static String getClassName(Class<?> clazz, boolean isSimple) {
        if (null == clazz) {
            return null;
        }
        return isSimple ? clazz.getSimpleName() : clazz.getName();
    }

    public static String getClassName(Object obj, boolean isSimple) {
        if (null == obj) {
            return null;
        }
        final Class<?> clazz = obj.getClass();
        return getClassName(clazz, isSimple);
    }

    public static <T> T getInstance(Class<T> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return clazz.getConstructor().newInstance();
    }

    public static Short toShort(Integer source, Short def) {
        if (source == null) {
            return def;
        }
        return source.shortValue();
    }

    public static Class<?> toClassConfident(String name, ClassLoader classLoader) {
        try {
            return loadClass(name, getClassLoaders(classLoader));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        return loadClass(className, getClassLoaders(ClassUtil.class.getClassLoader()));
    }

    public static Class<?> loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        ClassLoader[] classLoaders = getClassLoaders(classLoader);
        return loadClass(className, classLoaders);
    }

    private static Class<?> loadClass(String name, ClassLoader... classLoaders) throws ClassNotFoundException {
        for (ClassLoader classLoader : classLoaders) {
            if (classLoader == null) {
                continue;
            }
            try {
                return Class.forName(name, true, classLoader);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException("未发现类：" + name);
    }

    public static ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        return new ClassLoader[]{
                classLoader,
                Thread.currentThread().getContextClassLoader(),
                ClassUtil.class.getClassLoader(),
                ClassLoader.getSystemClassLoader()
        };
    }
}
