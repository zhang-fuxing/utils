package com.zhangfuxing.tools.common;

import com.zhangfuxing.tools.util.RefUtil;
import java.util.function.BiConsumer;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/7/11
 * @email zhangfuxing1010@163.com
 */
public class BuilderFactory<E> {
    private E target;

    /**
     * 创建一个构建工厂实例对象，可对指定类型进行构建
     *
     * @param clazz 需要构建的对象类型
     * @param <E>   类型E
     * @return 构建工厂
     */
    public static <E> BuilderFactory<E> create(Class<E> clazz) {
        E e = RefUtil.newInstance(clazz);
        BuilderFactory<E> factory = new BuilderFactory<>();
        factory.target = e;
        return factory;
    }

    /**
     * 填充需要构建的对象的属性
     *
     * @param setter 设置需要构建的目标对象的属性设置方法
     * @param value  属性值
     * @param <U>    值的类型 U
     * @return 构建工厂
     */
    public <U> BuilderFactory<E> setter(BiConsumer<E, U> setter, U value) {
        setter.accept(target, value);
        return this;
    }

    /**
     * 完成构建并获取构建的对象
     *
     * @return 构建的目标对象实例
     */
    public E build() {
        return this.target;
    }
}
