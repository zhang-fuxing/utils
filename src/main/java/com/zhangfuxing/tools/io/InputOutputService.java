package com.zhangfuxing.tools.io;

import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/1/5
 * @email zhangfuxing@kingshine.com.cn
 */
public class InputOutputService {
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] buffer;

    public InputOutputService() {
        buffer = new byte[1024 * 1024 * 4];
    }

    public InputOutputService in(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    public InputOutputService out(OutputStream outputStream) {
        this.outputStream = outputStream;
        return this;
    }

    public InputOutputService buf(byte[] buffer) {
        this.buffer = buffer;
        return this;
    }

    public void write(boolean isClose) throws IOException {
        if (inputStream == null || outputStream == null) {
            throw new IllegalArgumentException("inputStream or outputStream is null");
        }
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        if (isClose) {
            close(outputStream, inputStream);
        }
    }

    public void close(Closeable... closeable) {
        if (closeable == null) {
            return;
        }
        for (Closeable close : closeable) {
            try {
                if (close == null) {
                    continue;
                }
                close.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
