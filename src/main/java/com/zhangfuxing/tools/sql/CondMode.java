package com.zhangfuxing.tools.sql;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/25
 * @email zhangfuxing1010@163.com
 */
public enum CondMode {
    /**
     * a = ?
     */
    EQ,
    /**
     * a <> ?
     */
    NE,
    /**
     * a < ?
     */
    LT,
    /**
     * a <= ?
     */
    LE,
    /**
     * a > ?
     */
    GT,
    /**
     * a >= ?
     */
    GE,
    /**
     * a in(?...)
     */
    IN,
    /**
     * a like '%?%'
     */
    LIKE,
    /**
     * a is null
     */
    IS_NULL,
    /**
     * a is not null
     */
    IS_NOT_NULL
}
