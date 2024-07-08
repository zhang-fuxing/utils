package com.zhangfuxing.tools;

import com.zhangfuxing.tools.file.DataSizeUnit;
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
        DataSizeUnit unit = DataSizeUnit.parseUnit("123.1 pb");
        double v = unit.parseSize("123.1 pb");
        System.out.printf("%f",v);
        System.out.println();
        System.out.println(Fs.formatSize((long) v));
    }

}
