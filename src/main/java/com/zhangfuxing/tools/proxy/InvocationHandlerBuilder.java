package com.zhangfuxing.tools.proxy;

import java.lang.reflect.InvocationHandler;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/6
 * @email zhangfuxing1010@163.com
 */
public class InvocationHandlerBuilder {
    private Object target;

    ProxyInvocationHandler proxyInvocationHandler;

    public InvocationHandlerBuilder target(Object target) {
        this.target = target;
        return this;
    }

    public InvocationHandlerBuilder handler(ProxyInvocationHandler proxyInvocationHandler) {
        this.proxyInvocationHandler = proxyInvocationHandler;
        return this;
    }

    public InvocationHandler  build() {
        if (target == null) {
            throw new IllegalArgumentException("target cannot be null");
        }
        if (proxyInvocationHandler == null) {
            throw new IllegalArgumentException("proxyInvocationHandler cannot be null");
        }
        return (proxy, method, args) -> proxyInvocationHandler.invoke(target, method, args);
    }

}
