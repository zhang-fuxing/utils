package com.zhangfuxing.tools.plus;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/27
 * @email zhangfuxing1010@163.com
 */
public interface ThreeConsumer<T, U, I> {
    void accept(T t, U u, I i);
}
