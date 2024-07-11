package com.zhangfuxing.tools;

import com.zhangfuxing.tools.file.Fs;
import com.zhangfuxing.tools.spring.ioc.Spring;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/22
 * @email zhangfuxing1010@163.com
 */
@Spring
public class MainClass {

    public static void main(String[] args) {
        System.out.println(Fs.parseSize("1231233 m"));
    }

}
