package com.zhangfuxing.tools.spring.ioc;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/28
 * @email zhangfuxing1010@163.com
 */
@Documented
@Inherited
@Configurable
@Configuration
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Spring {
    Class<?>[] value() default {};
    String[] scanPackages() default {};
}
