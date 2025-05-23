package com.zhangfuxing.tools.rpc.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/15
 * @email zhangfuxing1010@163.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RpcClient {
    String schema() default "http";

    String host() default "127.0.0.1";

    int port() default 80;

    String domain() default "";

    String serviceName() default "";

    String[] headers() default {};

    long timeout() default -1;

    int maxRetries() default 3;
}
