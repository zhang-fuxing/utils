package com.zhangfuxing.tools.util;

import com.zhangfuxing.tools.io.InputOutputService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/1/5
 * @email zhangfuxing@kingshine.com.cn
 */
public class IoUtil {
    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        new InputOutputService()
                .in(inputStream)
                .out(outputStream)
                .write(false);
    }

    public static void copy(InputStream inputStream, OutputStream outputStream, boolean isClose) throws IOException {
        new InputOutputService()
                .in(inputStream)
                .out(outputStream)
                .write(isClose);
    }
}
