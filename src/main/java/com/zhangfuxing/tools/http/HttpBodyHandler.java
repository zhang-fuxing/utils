package com.zhangfuxing.tools.http;

import java.io.File;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * HttpResponse.BodyHandlers enum
 *
 * @author 张福兴
 * @version 1.0
 * @date 2024/1/4
 * @email zhangfuxing@kingshine.com.cn
 */
public enum HttpBodyHandler {
    /**
     * 字符串
     */
    STRING(String.class, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)),
    /**
     * 字节流
     */
    BYTES(byte[].class, HttpResponse.BodyHandlers.ofByteArray()),
    /**
     * 输入流
     */
    INPUT_STREAM(InputStream.class, HttpResponse.BodyHandlers.ofInputStream());


    public final HttpResponse.BodyHandler<?> bodyHandler;
    public final Class<?> type;

    HttpBodyHandler(Class<?> type, HttpResponse.BodyHandler<?> bodyHandler) {
        this.type = type;
        this.bodyHandler = bodyHandler;
    }

    public static HttpBodyHandler of(Class<?> type) {
        for (HttpBodyHandler responseType : HttpBodyHandler.values()) {
            if (responseType.type.equals(type)) {
                return responseType;
            }
        }
        return STRING;
    }
}
