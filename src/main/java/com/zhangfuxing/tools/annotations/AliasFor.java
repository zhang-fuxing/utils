package com.zhangfuxing.tools.annotations;

import java.lang.annotation.*;

/**
 * 模仿 Spring 的 AliasFor
 *
 * @author 张福兴
 * @version 1.0
 * @date 2025/4/21
 * @email zhangfuxing1010@163.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AliasFor {


	@AliasFor("attribute")
	String value() default "";


	@AliasFor("value")
	String attribute() default "";


	Class<? extends Annotation> annotation() default Annotation.class;
}
