package com.zhangfuxing.tools.rpc.error;

/**
 * RPC 错误处理接口
 */
public interface ErrorHandler {
    /**
     * 处理错误
     *
     * @param error 异常
     * @param serviceName 服务名称
     * @param methodName 方法名称
     * @param args 方法参数
     */
    void handleError(Throwable error, String serviceName, String methodName, Object[] args);

    /**
     * 是否重试该错误
     *
     * @param error 异常
     * @return true 表示可以重试，false 表示不重试
     */
    boolean shouldRetry(Throwable error);
} 