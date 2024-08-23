package com.zhangfuxing.tools.proxy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/6
 * @email zhangfuxing1010@163.com
 */
@SuppressWarnings({"unused", "unchecked"})
public class ProxyTools {
    static ClassFileVersion version = ClassFileVersion.JAVA_V8;

    public static void setClassFileVersion(ClassFileVersion version) {
        ProxyTools.version = version;
    }

    public static <T, U extends T> T newProxyInstance(U target, ProxyInvocationHandler handler) {
        Class<?>[] interfaces = target.getClass().getInterfaces();

        if (interfaces.length == 0) {
            throw new IllegalArgumentException("target must implement at least one interface");
        }
        return (T) Proxy.newProxyInstance(target.getClass().getClassLoader(), interfaces,
                toInvocationHandler(target, handler));
    }

    @SuppressWarnings("unchecked")
    public static <T, U extends T> T newProxyInstance(Class<T> interfaceClass, U target, ProxyInvocationHandler handler) {
        if (interfaceClass == null) {
            throw new NullPointerException("interfaceClass");
        }
        if (handler == null) {
            throw new NullPointerException("handler");
        }
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass},
                toInvocationHandler(target, handler));
    }

    public static <T> T newProxyInstance(T instance, ElementMatcher<? super MethodDescription> matcher, ProxyInvocationHandler handler) {
        return (T) newProxyInstance(ProxyTools.version, instance.getClass(), matcher, toInvocationHandler(instance, handler));
    }

    public static <T> T newProxyInstance(ClassFileVersion version, Class<T> subclass,
                                         ElementMatcher<? super MethodDescription> matcher,
                                         InvocationHandler handler) {
        DynamicType.Unloaded<T> make = new ByteBuddy(version)
                .subclass(subclass)
                .method(matcher)
                .intercept(InvocationHandlerAdapter.of(handler))
                .make();
        try (make) {
            return make.load(subclass.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("请提供无参构造函数", e);
        }
    }

    public static InvocationHandlerBuilder buildHandler() {
        return new InvocationHandlerBuilder();
    }


    private static <T, U extends T> InvocationHandler toInvocationHandler(U target, ProxyInvocationHandler handler) {
        return buildHandler()
                .target(target)
                .handler(handler)
                .build();
    }
}
