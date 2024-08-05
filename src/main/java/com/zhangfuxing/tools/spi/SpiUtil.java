package com.zhangfuxing.tools.spi;

import com.zhangfuxing.tools.util.ClassUtil;

import java.util.*;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/1
 * @email zhangfuxing1010@163.com
 */
public class SpiUtil {

    public static <T> T loadFirst(Class<T> clazz) {
        return loadFirst(clazz, null, null);
    }

    public static <T> T loadFirst(Class<T> clazz, T defaultValue) {
        return loadFirst(clazz, null, defaultValue);
    }

    public static <T> T loadFirst(Class<T> clazz, ClassLoader classLoader) {
        return loadFirst(clazz, classLoader, null);
    }

    public static <T> T loadFirst(Class<T> clazz, ClassLoader classLoader, T defaultValue) {
        Objects.requireNonNull(clazz);
        ServiceLoader<T> serviceLoader = loadService(clazz, Objects.requireNonNullElse(classLoader, clazz.getClassLoader()));
        Iterator<T> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            try {
                return iterator.next();
            } catch (Exception e) {
                // 记录错误日志
                System.err.println("Error loading service: " + e.getMessage());
            }
        }
        return defaultValue;
    }

    public static <T> Collection<T> loadAll(Class<T> clazz) {
        return loadAll(clazz, new ArrayList<>());
    }

    public static <T> Collection<T> loadAll(Class<T> clazz, Collection<T> collection) {
        Objects.requireNonNull(clazz);
        collection = Objects.requireNonNullElse(collection, new ArrayList<>());
        ServiceLoader<T> serviceLoader = loadService(clazz, clazz.getClassLoader());
        Iterator<T> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            try {
                collection.add(iterator.next());
            } catch (Exception e) {
                System.err.println("Error loading service: " + e.getMessage());
            }
        }
        return collection;
    }

    public static <T> ServiceLoader<T> loadService(Class<T> clazz, ClassLoader classLoader) {
        Objects.requireNonNull(clazz);
        ClassLoader[] classLoaders = ClassUtil.getClassLoaders(classLoader);
        ServiceLoader<T> load = null;
        for (ClassLoader loader : classLoaders) {
            try {
                load = ServiceLoader.load(clazz, loader);
                break;
            } catch (Exception e) {
                System.err.println("Error loading service: " + e.getMessage());
            }
        }
        if (load == null) throw new NoSuchElementException("No service loader found for class: " + clazz.getName());
        return load;
    }
}
