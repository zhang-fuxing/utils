package com.zhangfuxing.tools.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class CommonResponseHandler {
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

    @SuppressWarnings("unchecked")
    public static <T> HttpResponse.BodyHandler<T> of(Class<T> clazz) {
        if (clazz == String.class) {
            return (HttpResponse.BodyHandler<T>) STRING_UTF8;
        } else if (clazz == InputStream.class) {
            return (HttpResponse.BodyHandler<T>) INPUT_STREAM;
        } else if (clazz == byte[].class) {
            return (HttpResponse.BodyHandler<T>) BYTES;
        } else if (clazz == Void.class) {
            return (HttpResponse.BodyHandler<T>) DISCARDING;
        }
        Logger logger = LoggerFactory.getLogger(CommonResponseHandler.class);
        logger.warn("不支持自动解析 {} 类型响应, 将丢弃响应体,请添加参数 HttpResponse.BodyHandler<?> 到接口中", clazz.getName());
        return (HttpResponse.BodyHandler<T>) DISCARDING;
    }
}
