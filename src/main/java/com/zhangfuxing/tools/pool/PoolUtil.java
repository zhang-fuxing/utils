package com.zhangfuxing.tools.pool;

import java.util.function.Supplier;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/16
 * @email zhangfuxing1010@163.com
 */
public class PoolUtil {

    public static <T> Pools<T> build(final Supplier<T> objectConstruct, PoolsFactory<T> poolsFactory, PoolsConfig<T> config) {
        if (poolsFactory == null) {
            poolsFactory = new PoolsFactory<>() {
                @Override
                public T make() throws Exception {
                    return objectConstruct.get();
                }
            };
        }
        return new Pools<T>(poolsFactory, config);
    }

    public static <T> Pools<T> build(Supplier<T> objectConstruct) {
        return build(objectConstruct, new PoolsConfig<T>());
    }

    public static <T> Pools<T> build(final Supplier<T> objectConstruct, PoolsConfig<T> config) {
        return build(objectConstruct, null, config);
    }

    public static <T> Pools<T> build(PoolsFactory<T> poolsFactory) {
        return build(null, poolsFactory, new PoolsConfig<>());
    }
}
