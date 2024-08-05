package com.zhangfuxing.tools.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/1
 * @email zhangfuxing1010@163.com
 */
public class CloseUtil {

    public static void close(Closeable... closeables) {
        close(e -> {
        }, closeables);
    }

    public static void close(Consumer<Exception> exceptionHandler, Closeable... closeables) {
        Arrays.stream(closeables)
                .filter(Objects::nonNull)
                .forEach(r -> {
                    try {
                        r.close();
                    } catch (IOException e) {
                        exceptionHandler.accept(e);
                    }
                });
    }
}
