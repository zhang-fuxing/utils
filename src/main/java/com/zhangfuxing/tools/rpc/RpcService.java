package com.zhangfuxing.tools.rpc;

import com.zhangfuxing.tools.rpc.anno.RpcClient;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/15
 * @email zhangfuxing1010@163.com
 */
public class RpcService {
    private static final Map<Class<?>, Object> serviceCache = new ConcurrentHashMap<>();

    public static <T> T getService(Class<T> clazz) {
        return getService(clazz, null, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T getService(Class<T> clazz, String schema, String hostAndPort) {
        if (serviceCache.containsKey(clazz)) {
            return (T) serviceCache.get(clazz);
        }
        synchronized (RpcService.class) {
            if (!clazz.isAnnotationPresent(RpcClient.class)) {
                throw new IllegalArgumentException("指定类不是远程调用客户端，请添加 @RpcClient 到目标类上");
            }
            var handler = getRpcInvocationHandler(clazz, schema, hostAndPort);
            T serviceInstance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
            handler.setTarget(serviceInstance);
            serviceCache.put(clazz, serviceInstance);
            return serviceInstance;
        }
    }

    private static <T> RpcInvocationHandler getRpcInvocationHandler(Class<T> clazz, String schema, String hostAndPort) {
        RpcClient rpcClient = clazz.getAnnotation(RpcClient.class);
        var handler = new RpcInvocationHandler();
        String basURL;
        if (!rpcClient.domain().isBlank()) {
            basURL = rpcClient.domain();
        } else {
            basURL = Objects.requireNonNullElse(schema, rpcClient.schema()) + "://" +
                     Objects.requireNonNullElse(hostAndPort, rpcClient.host() + ":" + rpcClient.port());
        }
        handler.setBasURL(basURL);
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
}
