package com.zhangfuxing.tools.pool;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/15
 * @email zhangfuxing1010@163.com
 */
public class PoolsException extends RuntimeException{
    public PoolsException() {
    }

    public PoolsException(String message) {
        super(message);
    }

    public PoolsException(String message, Throwable cause) {
        super(message, cause);
    }

    public PoolsException(Throwable cause) {
        super(cause);
    }

    public PoolsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
