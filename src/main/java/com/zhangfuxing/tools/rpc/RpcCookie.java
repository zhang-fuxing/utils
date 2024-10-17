package com.zhangfuxing.tools.rpc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/17
 * @email zhangfuxing1010@163.com
 */
public class RpcCookie {
    public static final String COOKIE = "Cookie";
    private final Map<String, String> cookies;
    public RpcCookie() {
        cookies = new ConcurrentHashMap<>();
    }

    public RpcCookie put(String key, String value) {
        cookies.put(key, value);
        return this;
    }
    public String get(String key) {
        return cookies.get(key);
    }

    public Map<String, String> getCookies() {
        return cookies;
    }
}
