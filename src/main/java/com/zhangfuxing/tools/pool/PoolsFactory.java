package com.zhangfuxing.tools.pool;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/15
 * @email zhangfuxing1010@163.com
 */
public abstract class PoolsFactory<T> implements PooledObjectFactory<T> {
    Consumer<T> activateObjectCallback;
    Consumer<T> destroyObjectCallback;
    Predicate<T> objectAvailable;

    /**
     * 激活对象，即在返回object前要对改对象做什么
     *
     * @param callback 对对象的操作函数
     * @return 当前工厂对象
     */
    public PoolsFactory<T> pretreatment(Consumer<T> callback) {
        this.activateObjectCallback = callback;
        return this;
    }

    /**
     * 销毁对象时要对对象做什么，如 改对象是一个可关闭的资源对象，那么需要关闭资源，可在此方法中进行
     *
     * @param callback 销毁对象的操作函数
     * @return 当前工厂对象
     */
    public PoolsFactory<T> destroy(Consumer<T> callback) {
        this.destroyObjectCallback = callback;
        return this;
    }

    /**
     * 对象提供函数，要将什么对象添加到对象池，该对象要如何创建，由此方法决定
     *
     * @return 数据池对象
     */
    public abstract T make() throws Exception;

    /**
     * 检查该当前对象是否可用使用，如果不提供，则默认为不为空即可用使用
     *
     * @param objectAvailable 对象可用性的检查函数
     * @return 当前工厂对象
     */
    public PoolsFactory<T> available(Predicate<T> objectAvailable) {
        this.objectAvailable = objectAvailable;
        return this;
    }

    @Override
    public void activateObject(PooledObject<T> pooledObject) throws Exception {
        T object = pooledObject.getObject();
        Optional.ofNullable(this.activateObjectCallback)
                .ifPresent(c -> c.accept(object));
    }

    @Override
    public void destroyObject(PooledObject<T> pooledObject) throws Exception {
        T object = pooledObject.getObject();
        Optional.ofNullable(this.destroyObjectCallback)
                .ifPresent(c -> c.accept(object));
    }

    @Override
    public PooledObject<T> makeObject() throws Exception {
        return new DefaultPooledObject<T>(this.make());
    }

    @Override
    public void passivateObject(PooledObject<T> pooledObject) throws Exception {
    }

    @Override
    public boolean validateObject(PooledObject<T> pooledObject) {
        T object = pooledObject.getObject();
        return object != null && Optional.ofNullable(objectAvailable)
                .map(b -> b.test(object))
                .orElse(true);
    }


}
