package com.zhangfuxing.tools.http;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * 常见的Http响应体处理程序
 *
 * @author 张福兴
 * @version 1.0
 * @date 2024/1/4
 * @email zhangfuxing@kingshine.com.cn
 */
public class CommonHandler {
    /**
     * 字符串处理器
     */
    public static final HttpResponse.BodyHandler<String> STRING = HttpResponse.BodyHandlers.ofString();
    /**
     * utf-8编码的字符串处理器
     */
    public static final HttpResponse.BodyHandler<String> STRING_UTF8 = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
    /**
     * 字节输入流的处理器
     */
    public static final HttpResponse.BodyHandler<InputStream> INPUT_STREAM = HttpResponse.BodyHandlers.ofInputStream();
    /**
     * 字节数组的处理器
     */
    public static final HttpResponse.BodyHandler<byte[]> BYTES = HttpResponse.BodyHandlers.ofByteArray();
    /**
     * 丢弃响应体的处理器
     */
    public static final HttpResponse.BodyHandler<Void> DISCARDING = HttpResponse.BodyHandlers.discarding();
}
