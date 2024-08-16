package com.zhangfuxing.tools.serial;

import java.io.*;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/16
 * @email zhangfuxing1010@163.com
 */
public class BytesSerializeProvider<T extends Serializable> implements SerializeProvider<T, byte[]> {

    @Override
    public byte[] serialize(T serializeObj) {
        try (var bos = new ByteArrayOutputStream();
             var oos = new ObjectOutputStream(bos)) {
            oos.writeObject(serializeObj);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(byte[] serializeObj) {
        if (serializeObj == null) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serializeObj))) {
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
