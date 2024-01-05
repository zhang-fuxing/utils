package com.zhangfuxing.tools.common.fun;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2023/12/28
 * @email zhangfuxing@kingshine.com.cn
 */
public interface IFunction<T, R> {
    R apply(T var0, T var1);
}
