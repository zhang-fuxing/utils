package com.zhangfuxing.tools.rpc;

import com.zhangfuxing.tools.rpc.anno.RpcClient;
import com.zhangfuxing.tools.rpc.error.DefaultErrorHandler;
import com.zhangfuxing.tools.rpc.error.ErrorHandler;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/15
 * @email zhangfuxing1010@163.com
 */
@SuppressWarnings({"unchecked", "DuplicatedCode"})
public class RpcService {
    private static final Map<Class<?>, Object> serviceCache = new ConcurrentHashMap<>();
    private static ErrorHandler globalErrorHandler = new DefaultErrorHandler();

    public static void setGlobalErrorHandler(ErrorHandler errorHandler) {
        globalErrorHandler = errorHandler;
    }

    public static <T> T getService(Class<T> clazz) {
        return getService(clazz, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T getService(Class<T> clazz, String baseURL) {
        Object service = serviceCache.get(clazz);
        if (service != null) {
            return (T) service;
        }
        
        synchronized (RpcService.class) {
            service = serviceCache.get(clazz);
            if (service != null) {
                return (T) service;
            }
            
            if (!clazz.isAnnotationPresent(RpcClient.class)) {
                throw new IllegalArgumentException("指定类不是远程调用客户端，请添加 @RpcClient 到目标类上");
            }
            RpcClient rpcClient = clazz.getAnnotation(RpcClient.class);
            if (baseURL == null || baseURL.isBlank()) {
                String domain = rpcClient.domain();
                baseURL = domain.isBlank() ? rpcClient.schema() + "://" + rpcClient.host() + ":" + rpcClient.port() : domain;
            }
            var handler = getRpcInvocationHandler(baseURL);
            handler.setErrorHandler(globalErrorHandler);
            T serviceInstance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
            serviceCache.put(clazz, serviceInstance);
            return serviceInstance;
        }
    }

    public static <T> T getService(Class<T> clazz, String schema, String hostAndPort) {
        checkParams(schema, hostAndPort);
        return getService(clazz, schema + "://" + hostAndPort);
    }

    public static <T> T getService(Class<T> clazz, String schema, String host, int port) {
        checkParams(schema, host);
        return getService(clazz, schema + "://" + host + ":" + port);
    }

    public static <T> T getService(Class<T> clazz, String baseURL, int maxRetries) {
        Object service = serviceCache.get(clazz);
        if (service != null) {
            return (T) service;
        }
        
        synchronized (RpcService.class) {
            service = serviceCache.get(clazz);
            if (service != null) {
                return (T) service;
            }
            
            if (!clazz.isAnnotationPresent(RpcClient.class)) {
                throw new IllegalArgumentException("指定类不是远程调用客户端，请添加 @RpcClient 到目标类上");
            }
            RpcClient rpcClient = clazz.getAnnotation(RpcClient.class);
            if (baseURL == null || baseURL.isBlank()) {
                String domain = rpcClient.domain();
                baseURL = domain.isBlank() ? rpcClient.schema() + "://" + rpcClient.host() + ":" + rpcClient.port() : domain;
            }
            var handler = getRpcInvocationHandler(baseURL);
            handler.setMaxRetries(maxRetries);
            T serviceInstance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
            serviceCache.put(clazz, serviceInstance);
            return serviceInstance;
        }
    }

    private static <T> RpcInvocationHandler getRpcInvocationHandler(String baseURL) {
        var handler = new RpcInvocationHandler();
        handler.setBasURL(baseURL);
        return handler;
    }

    public static <T> T resetService(Class<T> clazz) {
        serviceCache.remove(clazz);
        return getService(clazz);
    }

    public static <T> T resetService(Class<T> clazz, String schema, String hostAndPort) {
        serviceCache.remove(clazz);
        return getService(clazz, schema, hostAndPort);
    }

    private static void checkParams(String schema, String hostAndPort) {
        if (schema == null || schema.isBlank()) {
            throw new IllegalArgumentException("schema 不能为空");
        }
        if (hostAndPort == null || hostAndPort.isBlank()) {
            throw new IllegalArgumentException("hostAndPort 不能为空");
        }
    }
}
