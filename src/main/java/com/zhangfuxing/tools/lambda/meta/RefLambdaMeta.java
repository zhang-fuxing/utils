package com.zhangfuxing.tools.lambda.meta;

import com.zhangfuxing.tools.util.ClassUtil;

import java.lang.invoke.SerializedLambda;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/7/29
 * @email zhangfuxing1010@163.com
 */
public class RefLambdaMeta implements LambdaMeta {

    final SerializedLambda lambda;
    final ClassLoader classLoader;

    public RefLambdaMeta(SerializedLambda lambda, ClassLoader classLoader) {
        this.lambda = lambda;
        this.classLoader = classLoader;
    }

    @Override
    public String implMethodName() {
        return lambda.getImplMethodName();
    }

    @Override
    public Class<?> instanceClass() {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(";")).replace("/", ".");
        return ClassUtil.toClassConfident(instantiatedType, this.classLoader);
    }
}
