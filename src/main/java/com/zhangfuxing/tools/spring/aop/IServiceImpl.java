package com.zhangfuxing.tools.spring.aop;

import org.springframework.stereotype.Service;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/28
 * @email zhangfuxing1010@163.com
 */
@Service
@AdviceCut
public class IServiceImpl implements IService {
    @Override
    public void doSomething() {
        System.out.println("I am doing something.");
    }
}
