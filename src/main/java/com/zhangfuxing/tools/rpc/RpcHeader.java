package com.zhangfuxing.tools.rpc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/17
 * @email zhangfuxing1010@163.com
 */
public class RpcHeader {
    private final Map<String, String> header = new ConcurrentHashMap<>();

    public RpcHeader set(String key, String value) {
        header.put(key, value);
        return this;
    }

    public Map<String, String> getHeader() {
        return header;
    }
}
