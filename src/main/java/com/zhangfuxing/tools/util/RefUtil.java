package com.zhangfuxing.tools.util;


import cn.hutool.core.collection.CollUtil;
import com.zhangfuxing.tools.common.enums.BasicType;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/1/24
 * @email zhangfuxing@kingshine.com.cn
 */
public class RefUtil {

    /**
     * 获取指定字段的值
     *
     * @param field 字段
     * @param o     对象
     * @return 返回指定字段的值
     */
    public static <T> Object get(Field field, T o) {
        if (field == null || o == null) {
            return null;
        }
        try {
            field.setAccessible(true);
            return field.get(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<Field> getAllFields(T obj) {
        return getAllFields(obj, f -> true);
    }

    public static <T> List<Field> getAllFields(T obj, Predicate<Field> filter) {
        if (obj == null) return null;
        return Arrays.stream(getAllFields(obj.getClass()))
                .filter(filter)
                .toList();
    }

    public static List<Field> getAllFields(Class<?> clazz, Predicate<Field> filter) {
        return Arrays.stream(getAllFields(clazz))
                .filter(filter)
                .toList();
    }

    public static <E> E newInstance(Class<E> clazz, Object... params) {
        assert clazz != null;
        E result;

        if (params == null || params.length == 0) {
            Constructor<E> constructor = null;
            try {
                constructor = clazz.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("该类未提供无参构造方法，无法实例化：class=" + clazz.getName());
            }
            constructor.setAccessible(true);
            try {
                result = constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("实例化失败", e);
            }
        } else {
            Constructor<E> constructor = null;
            Class<?>[] array = Arrays.stream(params).map(o -> {
                if (o instanceof Null<?> t) {
                    return t.clazz;
                }
                return o == null ? Object.class : o.getClass();
            }).toArray(Class[]::new);
            List<Constructor<?>> constructorList = Arrays.stream(clazz.getDeclaredConstructors())
                    .filter(c -> c.getParameterTypes().length == array.length)
                    .toList();
            if (CollUtil.isEmpty(constructorList)) {
                throw new RuntimeException("未找到ParamsSize=%d的构造方法".formatted(array.length));
            }

            for (Constructor<?> item : constructorList) {
                Class<?>[] parameterTypes = item.getParameterTypes();
                if (typeEq(parameterTypes, array)) {
                    //noinspection unchecked
                    constructor = (Constructor<E>) item;
                    break;
                }
            }

            if (constructor == null) {
                throw new RuntimeException("未找到符合参数类型的构造方法");
            }
            try {
                constructor.setAccessible(true);
                params = Arrays.stream(params)
                        .map(o -> o instanceof Null<?> ? null : o)
                        .toArray();
                result = constructor.newInstance(params);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("实例化失败", e);
            }
        }

        return result;
    }

    private static boolean typeEq(Class<?>[] types, Class<?>[] params) {
        if ((types == null || types.length == 0) && (params == null || params.length == 0)) {
            return true;
        }
        if (types == null || params == null) return false;
        if (types.length != params.length) {
            return false;
        }
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];
            Class<?> param = params[i];
            if (BasicType.isBasicType(type) && BasicType.isBasicType(param)) {
                if (BasicType.unwrapper(type) != BasicType.unwrapper(param)) return false;
            } else if (!type.isAssignableFrom(param)) {
                return false;
            }
        }

        return true;
    }

    public record Null<T>(Class<T> clazz) {
    }

    public static <T> Null<T> Null(Class<T> clazz) {
        return new Null<>(clazz);
    }

    public static Null<Object> Null() {
        return new Null<>(Object.class);
    }

    /**
     * 获取指定类的所有字段
     *
     * @param clazz 指定的类
     * @return 返回指定类的所有字段
     */
    public static Field[] getFields(Class<?> clazz) {
        return clazz == null ? null : clazz.getDeclaredFields();
    }


    /**
     * 获取指定对象的所有字段
     *
     * @param obj 要获取字段的对象
     * @return 指定对象的所有字段
     */
    public static Field[] getFields(Object obj) {
        return obj == null ? null : obj.getClass().getDeclaredFields();
    }


    /**
     * 获取指定类及其父类中所有的字段
     *
     * @param clazz 指定的类
     * @return 包含所有字段的数组
     */
    public static Field[] getAllFields(Class<?> clazz) {
        // 获取指定类的字段
        Field[] fields = clazz.getDeclaredFields();

        // 如果指定类有父类
        if (clazz.getSuperclass() != null) {
            // 创建新的字段数组，长度为指定类的字段个数加上父类字段个数之和
            fields = Arrays.copyOf(fields, fields.length + getAllFields(clazz.getSuperclass()).length);
            // 将父类的字段拷贝到指定类的字段数组中
            System.arraycopy(getAllFields(clazz.getSuperclass()), 0, fields, fields.length - getAllFields(clazz.getSuperclass()).length, getAllFields(clazz.getSuperclass()).length);
        }

        // 返回包含所有字段的数组
        return fields;
    }


    /**
     * 获取一个类继承的超类数量
     *
     * @param clazz 要检查的类
     * @return 超类数量
     */
    public static int getSuperClassCount(Class<?> clazz) {
        int count = 0;
        while (clazz.getSuperclass() != null) {
            count++;
            clazz = clazz.getSuperclass();
        }
        return count;
    }


    /**
     * 获取一个类的所有超类
     *
     * @param clazz 要获取超类的类对象
     * @return 包含所有超类的Class<?>数组
     */
    public static Class<?>[] getSuperClasses(Class<?> clazz) {
        Class<?>[] classes = new Class<?>[getSuperClassCount(clazz)]; // 创建一个Class数组用于存储超类
        int index = 0; // 记录数组索引
        while (clazz.getSuperclass() != null) { // 循环获取每个超类
            classes[index++] = clazz.getSuperclass(); // 将超类存储到数组中
            clazz = clazz.getSuperclass(); // 更新clazz为当前超类的超类
        }
        return classes; // 返回包含所有超类的数组
    }

    /**
     * 调用对象的方法
     *
     * @param obj        对象实例
     * @param methodName 方法名
     * @param args       参数列表
     * @return 调用方法的结果
     */
    public static Object invokeMethod(Object obj, String methodName, Object... args) {
        try {
            Method method = obj.getClass().getDeclaredMethod(methodName, getParameterTypes(args));
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 调用给定对象的给定方法，并传入给定参数。
     *
     * @param obj    要调用方法的对象
     * @param method 要调用的方法
     * @param args   方法参数
     * @return 调用方法的结果
     * @throws RuntimeException 如果方法调用抛出异常
     */
    public static Object invokeMethod(Object obj, Method method, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 根据类、方法名和参数动态调用静态方法，并返回其执行结果。
     *
     * @param clazz      包含静态方法的类
     * @param methodName 静态方法的名称
     * @param args       静态方法的参数列表
     * @return 静态方法执行后的返回值
     */
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Object... args) {
        try {
            // 获取与参数类型匹配的静态方法
            Method method = clazz.getDeclaredMethod(methodName, getParameterTypes(args));
            method.setAccessible(true);

            // 调用静态方法并获取结果
            return method.invoke(null, resolverArgs(args));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 调用静态方法并传入参数
     *
     * @param method 静态方法
     * @param args   参数列表
     * @return 调用返回结果
     */
    public static Object invokeStaticMethod(Method method, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(null, resolverArgs(args));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断给定的方法是否为静态方法
     *
     * @param method 待判断的方法
     * @return 如果给定的方法为静态方法，则返回true；否则返回false
     */
    public static boolean isStaticMethod(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * 获取方法参数的实际类型数组
     *
     * @param args 方法参数对象数组
     * @return 参数类型的Class数组
     */
    private static Class<?>[] getParameterTypes(Object... args) {
        if (args == null || args.length == 0) {
            return new Class<?>[0];
        }
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            Object item = args[i];
            if (item == null) {
                types[i] = Object.class;
            } else if (item instanceof Null<?> obj) {
                types[i] = obj.clazz;
            } else {
                types[i] = item.getClass();
            }
        }
        return types;
    }

    private static Object[] resolverArgs(Object[] args) {
        args = Arrays.stream(args)
                .map(o -> o instanceof Null<?> ? null : o)
                .toArray();
        return args;
    }
}
