package com.zhangfuxing.tools.http;

import java.io.InputStream;

/**
 * BodyHandler type
 *
 * @author 张福兴
 * @version 1.0
 * @date 2024/1/4
 * @email zhangfuxing@kingshine.com.cn
 */
public class ResponseType {
    public static final Class<String> STRING = String.class;
    public static final Class<byte[]> BYTES = byte[].class;
    public static final Class<InputStream> INPUT_STREAM = InputStream.class;
}
