package com.zhangfuxing.tools.pool;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/15
 * @email zhangfuxing1010@163.com
 */
public class Pools<T> extends GenericObjectPool<T> {
    public Pools(GenericObjectPoolConfig<T> poolConfig, PooledObjectFactory<T> factory) {
        this(factory, poolConfig);
    }

    public Pools(final PooledObjectFactory<T> factory, final GenericObjectPoolConfig<T> poolConfig) {
        super(factory, poolConfig);
    }

    public Pools(final PooledObjectFactory<T> factory) {
        super(factory);
    }


    @Override
    public void close() {
        destroy();
    }

    public void destroy() {
        try {
            super.close();
        } catch (RuntimeException e) {
            throw new PoolsException("Could not destroy the pool", e);
        }
    }

    public T getResource() {
        try {
            return super.borrowObject();
        } catch (PoolsException je) {
            throw je;
        } catch (Exception e) {
            throw new PoolsException("Could not get a resource from the pool", e);
        }
    }

    public void returnResource(final T resource) {
        if (resource == null) {
            return;
        }
        try {
            super.returnObject(resource);
        } catch (RuntimeException e) {
            throw new PoolsException("Could not return the resource to the pool", e);
        }
    }

    public void returnBrokenResource(final T resource) {
        if (resource == null) {
            return;
        }
        try {
            super.invalidateObject(resource);
        } catch (Exception e) {
            throw new PoolsException("Could not return the broken resource to the pool", e);
        }
    }

    @Override
    public void addObjects(int count) {
        try {
            for (int i = 0; i < count; i++) {
                addObject();
            }
        } catch (Exception e) {
            throw new PoolsException("Error trying to add idle objects", e);
        }
    }

    public void runOnPool(Consumer<T> action) {
        T resource = getResource();
        try {
            if (action == null) {
                return;
            }
            action.accept(resource);
        } finally {
            returnResource(resource);
        }
    }

    public <E> E callOnPool(Function<T,E> call) {
        T resource = getResource();
        try {
            if (call == null) {
                return null;
            }
            return call.apply(resource);
        } finally {
            returnResource(resource);
        }
    }
}
