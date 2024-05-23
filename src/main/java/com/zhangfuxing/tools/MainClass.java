package com.zhangfuxing.tools;

import com.zhangfuxing.tools.io.ReadResource;
import com.zhangfuxing.tools.spring.ioc.Spring;
import com.zhangfuxing.tools.spring.ioc.SpringLoader;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/22
 * @email zhangfuxing1010@163.com
 */
@Spring
public class MainClass {

    @Autowired
    ReadResource readResource;

    public static void main(String[] args) throws Exception {
        var context = SpringLoader.load(MainClass.class, args);
        context.getBean(MainClass.class)
                .test();
    }

    public void test() {
        String s = readResource.resetFilePath("H:/proxy.txt")
                .readByClasspath(false)
                .reloadStream()
                .readText();
        System.out.println(s);
    }

}
