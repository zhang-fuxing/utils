package com.zhangfuxing.tools.serial;

import java.io.File;
import java.io.Serializable;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/16
 * @email zhangfuxing1010@163.com
 */
public class SerializeUtil {

    public static File serialize(Serializable serializable, File file) {
        FileSerializeProvider<Serializable> fileSerialize = new FileSerializeProvider<>(file);
        return fileSerialize.serialize(serializable);
    }

    public static <T extends Serializable> T deserialize(File file) {
        FileSerializeProvider<T> fileSerialize = new FileSerializeProvider<>(file);
        return fileSerialize.deserialize(file);
    }

    public static byte[] serialize(Serializable serializable) {
        var serializer = new BytesSerializeProvider<>();
        return serializer.serialize(serializable);
    }

    public static <T extends Serializable> T deserialize(byte[] data) {
        return new BytesSerializeProvider<T>().deserialize(data);
    }
}
