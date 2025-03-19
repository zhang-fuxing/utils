package com.zhangfuxing.tools.rpc.error;

import com.zhangfuxing.tools.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpConnectTimeoutException;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * 默认错误处理器
 */
public class DefaultErrorHandler implements ErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(DefaultErrorHandler.class);
    
    // 可重试的异常类型
    private static final Set<Class<? extends Throwable>> RETRYABLE_EXCEPTIONS = Set.of(
            ConnectException.class,
            SocketTimeoutException.class,
            TimeoutException.class,
            HttpConnectTimeoutException.class
    );

    @Override
    public RpcException wrapException(Exception ex, String url, Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        String name = method.getName();
        String errorMethod = declaringClass.getName() + "." + name;

        RpcException rpcException = new RpcException("RPC调用异常: URL= %s error= %s, position: ".formatted(url, ex.getMessage()) + errorMethod, 5000, url, ex);
        if (ex instanceof ConnectException) {
            rpcException = new RpcException("连接异常: URL= %s error= %s, position: ".formatted(url, ex.getMessage()) + errorMethod, 50000, url, ex);
        } else if (ex instanceof HttpConnectTimeoutException) {
            rpcException = new RpcException("请求超时: URL= %s error= %s, position: ".formatted(url, ex.getMessage()) + errorMethod, 50005, url, ex);
        } else if (ex instanceof IOException) {
            rpcException = new RpcException("网络异常: URL= %s error= %s, position: ".formatted(url, ex.getMessage()) + errorMethod, 50001, url, ex);
        } else if (ex instanceof ClassNotFoundException) {
            rpcException = new RpcException("类型定义错误: URL= %s error= %s, position: ".formatted(url, ex.getMessage()) + errorMethod, 50002, url, ex);
        } else if (ex instanceof InterruptedException) {
            rpcException = new RpcException("请求被中断: URL= %s error= %s, position: ".formatted(url, ex.getMessage()) + errorMethod, 50003, url, ex);
        } else if (ex instanceof IllegalArgumentException) {
            rpcException = new RpcException("非法参数: URL= %s error= %s, position: ".formatted(url, ex.getMessage()) + errorMethod, 50004, url, ex);
        }

        return rpcException;
    }

    @Override
    public boolean shouldRetry(Throwable error) {
        if (error == null) {
            return false;
        }
        // 检查异常是否属于可重试异常
        return RETRYABLE_EXCEPTIONS.stream()
                .anyMatch(exceptionClass -> exceptionClass.isAssignableFrom(error.getClass()));
    }
} 