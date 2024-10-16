package com.zhangfuxing.tools.rpc;

import com.zhangfuxing.tools.rpc.anno.RpcClient;
import com.zhangfuxing.tools.rpc.anno.RpcMapping;
import com.zhangfuxing.tools.rpc.anno.RpcParam;

import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
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

    @SuppressWarnings({"unchecked", "SuspiciousInvocationHandlerImplementation"})
    public static <T> T getService(Class<T> clazz, String schema, String address) {
        if (serviceCache.containsKey(clazz)) {
            return (T) serviceCache.get(clazz);
        }
        if (!clazz.isAnnotationPresent(RpcClient.class)) {
            throw new IllegalArgumentException("指定类不是远程调用客户端，请添加 @RpcClient 到目标类上");
        }
        RpcClient rpcClient = clazz.getAnnotation(RpcClient.class);
        T serviceInstance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            Class<?> declaringClass = method.getDeclaringClass();
            String uri = Optional.ofNullable(declaringClass.getAnnotation(RpcMapping.class))
                    .map(RpcMapping::value)
                    .orElse("");
            RpcMapping rpcMapping = method.getAnnotation(RpcMapping.class);
            uri = uri + rpcMapping.value();
            StringJoiner urlParams = new StringJoiner("&");
            for (int i = 0; i < method.getParameters().length; i++) {
                Parameter parameter = method.getParameters()[i];
                RpcParam annotation = parameter.getAnnotation(RpcParam.class);
                if (annotation != null) {
                    String paraName = annotation.value();
                    urlParams.add(paraName + "=" + URLEncoder.encode(String.valueOf(args[i]), StandardCharsets.UTF_8));
                }
            }
            if (!urlParams.toString().isBlank()) {
                uri = uri + "?" + urlParams;
            }
            if (method.getReturnType() != String.class) {
                return null;
            }
            return Objects.requireNonNullElse(schema, rpcClient.schema()) +
                   "://" +
                   Objects.requireNonNullElse(address, rpcClient.address()) +
                   uri;
        });
        serviceCache.put(clazz, serviceInstance);
        return serviceInstance;
    }

    public static <T> T resetService(Class<T> clazz) {
        serviceCache.remove(clazz);
        return getService(clazz);
    }

    public static <T> T resetService(Class<T> clazz, String schema, String address) {
        serviceCache.remove(clazz);
        return getService(clazz, schema, address);
    }
}
