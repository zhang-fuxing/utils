package com.zhangfuxing.tools.sql;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/25
 * @email zhangfuxing1010@163.com
 */
public record Where(String col, Object value, CondMode condMode) {

    public static Where of(String col, Object value) {
        return new Where(col,value,CondMode.EQ);
    }

    public static Where of(String col, CondMode mode, Object value) {
        return new Where(col, value, mode);
    }
}
