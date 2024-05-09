package com.zhangfuxing.tools.chain;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/8
 * @email zhangfuxing1010@163.com
 */
public interface ChainHandler<T, R> {
    int DEFAULT_ORDER = Integer.MAX_VALUE;
    default int getOrder() {
        return DEFAULT_ORDER;
    }


    boolean matcher(T input);

    R handle(T input);
}
