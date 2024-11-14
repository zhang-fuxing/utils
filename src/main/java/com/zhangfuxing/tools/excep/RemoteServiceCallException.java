package com.zhangfuxing.tools.excep;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/11/14
 * @email zhangfuxing1010@163.com
 */
public class RemoteServiceCallException extends RuntimeException {
    public RemoteServiceCallException() {
    }

    public RemoteServiceCallException(String message) {
        super(message);
    }

    public RemoteServiceCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteServiceCallException(Throwable cause) {
        super(cause);
    }

    public RemoteServiceCallException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
