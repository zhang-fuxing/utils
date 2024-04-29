package com.zhangfuxing.tools.spring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 通过自定义注解进行AOP切面
 *
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/28
 * @email zhangfuxing1010@163.com
 */
@Aspect
@Component
@Order(1)
public class IServiceAnnoAdvice {

    private Map<String, AdviceAction> adviceActions;

    @Autowired(required = false)
    public void setAdviceActions(Map<String, AdviceAction> adviceActions) {
        this.adviceActions = adviceActions;
    }

    @Pointcut("@annotation(adviceCut) || @within(adviceCut)")
    public void point(AdviceCut adviceCut) {
    }

    @Around(value = "point(adviceCut)", argNames = "joinPoint,adviceCut")
    public Object around(ProceedingJoinPoint joinPoint, AdviceCut adviceCut) throws Throwable {
        System.out.println("anno advice before");
        Object result = joinPoint.proceed();
        System.out.println("anno advice after");

        return result;
    }

}
