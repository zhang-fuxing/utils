package com.zhangfuxing.tools.common.exception;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2023/12/28
 * @email zhangfuxing@kingshine.com.cn
 */
public class FlowChainException extends RuntimeException {
    public FlowChainException() {
    }

    public FlowChainException(String message) {
        super(message);
    }

    public FlowChainException(String message, Throwable cause) {
        super(message, cause);
    }

    public FlowChainException(Throwable cause) {
        super(cause);
    }

    public FlowChainException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
