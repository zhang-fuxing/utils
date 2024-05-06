package com.zhangfuxing.tools.proxy;

import java.lang.reflect.Method;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/6
 * @email zhangfuxing1010@163.com
 */
public interface ProxyInvocationHandler {
    Object invoke(Object target, Method method, Object[] args) throws Throwable;
}
