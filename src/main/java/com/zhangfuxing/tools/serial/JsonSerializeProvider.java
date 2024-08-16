package com.zhangfuxing.tools.serial;

import cn.hutool.json.JSONUtil;

import java.io.Serializable;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/16
 * @email zhangfuxing1010@163.com
 */
public class JsonSerializeProvider<T extends Serializable> implements SerializeProvider<T, String> {
    private final Class<T> type;

    public JsonSerializeProvider(Class<T> type) {
        this.type = type;
    }

    @Override
    public String serialize(T serializeObj) {
        return JSONUtil.toJsonStr(serializeObj);
    }

    @Override
    public T deserialize(String serializeObj) {
        return JSONUtil.toBean(serializeObj, type);
    }

}
