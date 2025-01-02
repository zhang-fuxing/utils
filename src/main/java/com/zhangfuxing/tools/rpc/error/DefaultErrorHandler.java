package com.zhangfuxing.tools.rpc.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
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
            TimeoutException.class
    );

    @Override
    public void handleError(Throwable error, String serviceName, String methodName, Object[] args) {
        log.error("RPC调用异常 - 服务: {}, 方法: {}, 参数: {}, 异常: {}",
                serviceName, methodName, Arrays.toString(args), error.getMessage(), error);
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