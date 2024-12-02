package com.zhangfuxing.tools.rpc;

import java.util.Map;
import java.util.Set;
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

    public String[] getHeaders() {
        String[] headers = new String[header.size() * 2];
        int i = 0;
        Set<String> keySet = header.keySet();
        for (String key : keySet) {
            headers[i++] = key;
            headers[i++] = header.get(key);
        }
        return headers;
    }

}
