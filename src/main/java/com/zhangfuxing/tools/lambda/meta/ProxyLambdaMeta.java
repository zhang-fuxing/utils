package com.zhangfuxing.tools.lambda.meta;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.lang.reflect.Proxy;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/7/29
 * @email zhangfuxing1010@163.com
 */
public class ProxyLambdaMeta implements LambdaMeta {
    final Class<?> clazz;
    final String methodName;

    public ProxyLambdaMeta(Proxy fun) {
        MethodHandle dmh = MethodHandleProxies.wrapperInstanceTarget(fun);
        Executable executable = MethodHandles.reflectAs(Executable.class, dmh);
        this.clazz = executable.getDeclaringClass();
        this.methodName = executable.getName();
    }

    @Override
    public String toString() {
        return "ProxyLambdaMeta{" +
               "clazz=" + clazz +
               ", methodName='" + methodName + '\'' +
               '}';
    }

    @Override
    public String implMethodName() {
        return methodName;
    }

    @Override
    public Class<?> instanceClass() {
        return clazz;
    }
}
