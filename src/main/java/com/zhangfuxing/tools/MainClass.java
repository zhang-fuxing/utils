package com.zhangfuxing.tools;

import com.zhangfuxing.tools.file.Fs;
import com.zhangfuxing.tools.spring.ioc.Spring;

import java.io.IOException;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/22
 * @email zhangfuxing1010@163.com
 */
@Spring
public class MainClass {

    public static void main(String[] args) throws IOException {
        String size = Fs.formatSize(12333312312441L);
        System.out.println(size);
        System.out.println(Fs.parseSize(size));
    }

}
