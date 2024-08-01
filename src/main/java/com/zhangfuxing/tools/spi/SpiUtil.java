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
        Objects.requireNonNull(clazz);
        return loadFirst(clazz, null, null);
    }

    public static <T> T loadFirst(Class<T> clazz, T defaultValue) {
        Objects.requireNonNull(clazz);
        return loadFirst(clazz, null, defaultValue);
    }

    public static <T> T loadFirst(Class<T> clazz, ClassLoader classLoader) {
        Objects.requireNonNull(clazz);
        return loadFirst(clazz, classLoader, null);
    }

    public static <T> T loadFirst(Class<T> clazz, ClassLoader classLoader, T defaultValue) {
        Objects.requireNonNull(clazz);
        ServiceLoader<T> serviceLoader = loadService(clazz, Objects.requireNonNullElse(classLoader, clazz.getClassLoader()));
        Iterator<T> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            try {
                return iterator.next();
            } catch (Exception ignored) {
            }
        }
        return defaultValue;
    }

    public static <T> Collection<T> loadAll(Class<T> clazz) {
        Objects.requireNonNull(clazz);
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
            } catch (Exception ignored) {
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
            } catch (Exception ignored) {
            }
        }
        if (load == null) throw new NoSuchElementException();
        return load;
    }
}
