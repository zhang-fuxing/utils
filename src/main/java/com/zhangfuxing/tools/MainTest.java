package com.zhangfuxing.tools;

import com.zhangfuxing.tools.chain.ChainHandlerRegister;
import com.zhangfuxing.tools.spring.ioc.Spring;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/25
 * @email zhangfuxing1010@163.com
 */
@Spring
public class MainTest {

    enum Color {
        RED, GREEN, BLUE
    }


    public static void main(String[] args) throws Exception {
        var execute = ChainHandlerRegister.<Color, String>newInstance()
                .register(color -> true, color -> color + ":红色")
                .register(color -> true, color -> color + ":蓝色")
                .register(color -> true, color -> color + ":绿色")
                .doAllMatching(Color.BLUE);
        System.out.println(execute);
    }

}
