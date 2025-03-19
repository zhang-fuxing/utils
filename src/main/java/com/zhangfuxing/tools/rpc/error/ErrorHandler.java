package com.zhangfuxing.tools.rpc.error;

import com.zhangfuxing.tools.rpc.RpcException;

import java.lang.reflect.Method;

/**
 * RPC 错误处理接口
 */
public interface ErrorHandler {
    RpcException wrapException(Exception ex, String url, Method method);

    /**
     * 是否重试该错误
     *
     * @param error 异常
     * @return true 表示可以重试，false 表示不重试
     */
    boolean shouldRetry(Throwable error);
} 