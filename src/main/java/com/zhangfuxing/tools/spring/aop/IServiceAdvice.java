package com.zhangfuxing.tools.spring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 通过表达式进行AOP的切面，
 *
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/28
 * @email zhangfuxing1010@163.com
 */
@Aspect
@Component
@Order(10)
public class IServiceAdvice {

    @Pointcut("execution(* com.zhangfuxing.tools.spring.aop.IService.*(..))")
    public void point() {
    }

    @Around(value = "point()", argNames = "joinPoint")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("before advice");
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            System.out.println("exception advice");
            throw e;
        }
        System.out.println("after advice");
        return result;
    }
}
