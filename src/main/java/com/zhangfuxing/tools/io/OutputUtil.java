package com.zhangfuxing.tools.io;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/26
 * @email zhangfuxing1010@163.com
 */
public class OutputUtil {

    public static void write(InputStream in, File file) {
        try {
            write(in, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(InputStream in, OutputStream out) {
        int len;
        byte[] buf = new byte[1024 * 4];
        try {
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void output(String content, File file, Charset charset) {
        write(InputUtil.byteStream(content.getBytes(charset)), file);
    }

    public static void write(String content, File file) {
        output(content, file, StandardCharsets.UTF_8);
    }


}
