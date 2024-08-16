package com.zhangfuxing.tools.serial;

import java.io.Serializable;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/16
 * @email zhangfuxing1010@163.com
 */
public interface SerializeProvider<T extends Serializable, R> {

    /**
     * 将对象序列化的方法，传入的参数必须实现 Serializable 接口
     * <p>
     * R 指定了序列化后的返回值类型
     *
     * @param serializeObj 需要序列化的对象
     * @return 序列化后的对象
     */
    R serialize(T serializeObj);

    /**
     * 将反序列化的对象进行反序列化
     *
     * @param serializeObj 待反序列化的对象，可能是 byte[], 也可能是 一个文件等
     * @return 真实对象类型
     */
    T deserialize(R serializeObj);

    /**
     * 设置序列化时写出的文件资源，如果通过 SerializeProvider 对象调用，需要设置该属性
     * <p>
     * 设置反序列化时需要的资源，不强制要求实现，具体看序列化提供者的需求，当使用默认的文件序列化通过者时，可以通过该方法设置反序列化需要的文件对象
     *
     * @param resource 资源对象
     */
    default void setResource(R resource) {
    }
}
