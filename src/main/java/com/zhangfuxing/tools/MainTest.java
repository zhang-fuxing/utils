package com.zhangfuxing.tools;

import com.zhangfuxing.tools.file.SimpleFileUtils;
import com.zhangfuxing.tools.spring.ioc.Spring;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/25
 * @email zhangfuxing1010@163.com
 */
@Spring
public class MainTest {

    public static void main(String[] args) throws Exception {
    }

    static void bioCp(String source, String target) throws IOException {
        try (var fis = new FileInputStream(source);
             var fos = new FileOutputStream(target)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }

    private static void channelCp(String source, String target) throws IOException {
        FileChannel inChannel = SimpleFileUtils.getChannel(source, StandardOpenOption.READ);
        FileChannel outChannel = SimpleFileUtils.getChannel(target, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        try (inChannel; outChannel) {
            outChannel.transferFrom(inChannel, 0, inChannel.size());
        }
    }

}
