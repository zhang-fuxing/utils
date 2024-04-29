package com.zhangfuxing.tools.spring.aop;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/28
 * @email zhangfuxing1010@163.com
 */
public interface AdviceAction {
    default int serial() {
        return 0;
    }
    Object intercept(ProceedingJoinPoint joinPoint, AdviceCut adviceCut, Object result) throws Throwable;
}
