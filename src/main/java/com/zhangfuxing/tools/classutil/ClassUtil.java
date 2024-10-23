package com.zhangfuxing.tools.classutil;

import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

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

    public static ClassLoader getClassLoaders(Class<?> clazz) {
        return clazz.getClassLoader();
    }

    public static boolean isPrimitive(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return BasicType.PRIMITIVE_WRAPPER_MAP.containsKey(clazz);
    }

    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return BasicType.WRAPPER_PRIMITIVE_MAP.containsKey(clazz);
    }

    public static boolean isBasicType(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return isPrimitive(clazz) || isPrimitiveWrapper(clazz);
    }

    public static URLClassLoader loadJar(String... jarPaths) {
        try {
            URL[] loadUrls = new URL[jarPaths.length];
            for (int i = 0, jarPathsLength = jarPaths.length; i < jarPathsLength; i++) {
                String jarPath = jarPaths[i];
                URL url = new URL("jar:file:" + jarPath + "!/");
                loadUrls[i] = url;
            }
            return new URLClassLoader(loadUrls);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getClass(Type type) {
        if (null != type) {
            if (type instanceof Class) {
                return (Class<?>) type;
            } else if (type instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) type).getRawType();
            } else if (type instanceof TypeVariable) {
                Type[] bounds = ((TypeVariable<?>) type).getBounds();
                if (bounds.length == 1) {
                    return getClass(bounds[0]);
                }
            } else if (type instanceof WildcardType) {
                final Type[] upperBounds = ((WildcardType) type).getUpperBounds();
                if (upperBounds.length == 1) {
                    return getClass(upperBounds[0]);
                }
            }
        }
        return null;
    }

    public static Class<?> getGenericClass(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length == 1) {
                return getClass(actualTypeArguments[0]);
            }
        } else if (type instanceof WildcardType) {
            final Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getClass(upperBounds[0]);
            }
        } else if (type instanceof TypeVariable) {
            final Type[] bounds = ((TypeVariable<?>) type).getBounds();
            if (bounds.length == 1) {
                return getClass(bounds[0]);
            }
        } else if (type instanceof GenericArrayType genericArrayType) {
            final Type componentType = genericArrayType.getGenericComponentType();
            if (componentType instanceof ParameterizedType) {
                final Type[] actualTypeArguments = ((ParameterizedType) componentType).getActualTypeArguments();
                if (actualTypeArguments.length == 1) {
                    return getClass(actualTypeArguments[0]);
                }
            }
        } else if (type instanceof Class<?> c) {
            return c;
        }
        return null;
    }

}
