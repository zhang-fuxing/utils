package com.zhangfuxing.tools.spring.ioc;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/28
 * @email zhangfuxing1010@163.com
 */
public class SpringApplicationListener1 implements ApplicationListener {
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        System.out.println("SpringApplicationListener1 onApplicationEvent...");
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
