package com.zhangfuxing.tools;

import com.zhangfuxing.tools.spring.ioc.Spring;
import com.zhangfuxing.tools.spring.ioc.Springs;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/25
 * @email zhangfuxing1010@163.com
 */
@Spring
public class MainTest {

    public static void main(String[] args)  {
        var context = Springs.start(MainTest.class, args);

    }

}
