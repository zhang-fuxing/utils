package com.zhangfuxing.tools.lambda;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/12/6
 * @email zhangfuxing1010@163.com
 */
public class LambdaUtil {

    public static <T> String getMethodName(SerializableFunction<T, ?> sf) {
        LambdaMetaInfo lambMetaInfo = getLambMetaInfo(sf);
        return lambMetaInfo.getImplMethodName();
    }

    public static <T> String getFiledName(SerializableFunction<T, ?> sf) {
        return getFiledName(sf, true);
    }

    public static <T> String getFiledName(SerializableFunction<T, ?> sf, boolean strict) {
        if (strict) {
            Field field = getField(sf);
            return Optional.ofNullable(field).map(Field::getName).orElse(null);
        }
        LambdaMetaInfo lambMetaInfo = getLambMetaInfo(sf);

        String methodName = lambMetaInfo.getImplMethodName();
        if (methodName.startsWith("get")) {
            String temp = methodName.substring(3);
            methodName = temp.substring(0, 1).toLowerCase() + temp.substring(1);
        } else if (methodName.startsWith("is")) {
            String temp = methodName.substring(2);
            methodName = temp.substring(0, 1).toLowerCase() + temp.substring(1);

        }
        return methodName;
    }

    public static <T> Field getField(SerializableFunction<T, ?> sf) {
        LambdaMetaInfo lambMetaInfo = getLambMetaInfo(sf);
        try {
            Class<?> instanceClass = lambMetaInfo.getInstanceClass();
            String methodName = lambMetaInfo.getImplMethodName();
            boolean isGetMethod = methodName.startsWith("get");

            // 如果不是标准的 get isXX 方法，无法获取对应的字段
            if (!isGetMethod && !methodName.startsWith("is")) {
                return getField(instanceClass, methodName);
            }

            if (isGetMethod) {
                methodName = methodName.substring(3);
            } else {
                methodName = methodName.substring(2);
            }
            var filedName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
            return getField(instanceClass, filedName, methodName, filedName.toLowerCase());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Field getField(Class<?> clazz, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                return getField(clazz, fieldName);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        return clazz.getDeclaredField(fieldName);
    }


    private static LambdaMetaInfo getLambMetaInfo(SerializableFunction<?, ?> sf) {
        LambdaMetaInfo result;
        try {
            if (sf instanceof Proxy proxy) {
                result = new ProxyLambdaMetaInfoImpl(proxy);
            } else {
                result = new ReflectLambdaMetaInfoImpl(sf);
            }
        } catch (Exception e) {
            result = new SerialLambdaMetaInfoImpl(sf);
        }
        return result;
    }

    public static <T> String getName(SerializableFunction<T, ?> keyExt) {
        return getFiledName(keyExt);
    }

    /**
     * 可序列化的函数式接口
     *
     * @param <T> 泛型T
     * @param <R> 泛型R
     */
    @FunctionalInterface
    public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
    }

    /**
     * Lambda 表达式的元信息
     */
    private interface LambdaMetaInfo extends Serializable {
        String getImplMethodName();

        Class<?> getInstanceClass() throws ClassNotFoundException;
    }

    /**
     * 代理对象的 Lambda 元信息实现
     */
    private static class ProxyLambdaMetaInfoImpl implements LambdaMetaInfo {

        final Class<?> instanceClass;
        final String implMethodName;

        private ProxyLambdaMetaInfoImpl(Proxy proxy) {
            MethodHandle methodHandle = MethodHandleProxies.wrapperInstanceTarget(proxy);
            Executable executable = MethodHandles.reflectAs(Executable.class, methodHandle);
            this.instanceClass = executable.getDeclaringClass();
            this.implMethodName = executable.getName();
        }

        @Override
        public String getImplMethodName() {
            return this.implMethodName;
        }

        @Override
        public Class<?> getInstanceClass() {
            return this.instanceClass;
        }
    }

    /**
     * 通过反射获取的 Lambda 元信息实现
     */
    private static class ReflectLambdaMetaInfoImpl implements LambdaMetaInfo {
        final SerializedLambda serializedLambda;
        final ClassLoader classLoader;

        private ReflectLambdaMetaInfoImpl(SerializableFunction<?, ?> sf) {
            try {
                this.classLoader = sf.getClass().getClassLoader();
                Method writeReplace = sf.getClass().getDeclaredMethod("writeReplace");
                writeReplace.setAccessible(true);
                serializedLambda = (SerializedLambda) writeReplace.invoke(sf);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getImplMethodName() {
            return serializedLambda.getImplMethodName();
        }

        @Override
        public Class<?> getInstanceClass() throws ClassNotFoundException {
            // 获取实列化该方法的类型的字符串
            String instantiatedMethodType = serializedLambda.getInstantiatedMethodType();
            // 转换为java 包的形式
            String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(";")).replace("/", ".");

            return loadClass(instantiatedType, serializedLambda.getClass().getClassLoader());
        }
    }

    /**
     * 可序列化的 Lambda 元信息实现，将 Lambda 序列化后再获取其元信息
     */
    private static class SerialLambdaMetaInfoImpl implements LambdaMetaInfo {

        @Serial
        private static final long serialVersionUID = 1L;

        // 可序列化的 Lambda 对象
        final SerializedLambda instance;


        private SerialLambdaMetaInfoImpl(SerializableFunction<?, ?> serializable) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                // 序列化 Lambda 对象
                oos.writeObject(serializable);
                oos.flush();
                // 从序列化对象中读取对象
                Object o = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
                instance = (SerializedLambda) o;
            } catch (Exception e) {
                throw new RuntimeException("无法提取Lambda的信息", e);
            }
        }

        @Override
        public String getImplMethodName() {
            return instance.getImplMethodName();
        }

        @Override
        public Class<?> getInstanceClass() throws ClassNotFoundException {
            String instantiatedMethodType = instance.getInstantiatedMethodType();
            String instanceType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(";")).replace("/", ".");
            return loadClass(instanceType, instance.getClass().getClassLoader());
        }
    }

    private static ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        return new ClassLoader[]{
                classLoader,
                Thread.currentThread().getContextClassLoader(),
                LambdaUtil.class.getClassLoader(),
                ClassLoader.getSystemClassLoader()
        };
    }

    private static Class<?> loadClass(String name, ClassLoader thisClassLoader) throws ClassNotFoundException {
        ClassLoader[] classLoaders = getClassLoaders(thisClassLoader);
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
}
