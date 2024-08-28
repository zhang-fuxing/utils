package com.zhangfuxing.tools.plus;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/27
 * @email zhangfuxing1010@163.com
 */
public interface BiEntry<K, F, V> {
    K getKey();

    F getField();

    V getValue();
}
