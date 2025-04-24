package com.zhangfuxing.tools.rpc.anno;

import com.zhangfuxing.tools.annotations.AliasFor;
import com.zhangfuxing.tools.rpc.Method;

import java.lang.annotation.*;

import static com.zhangfuxing.tools.rpc.Method.GET;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/15
 * @email zhangfuxing1010@163.com
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RpcMapping {
    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    Method method() default GET;

    String[] headers() default {"Content-Type: application/json"};

    long timeout() default -1;
}
