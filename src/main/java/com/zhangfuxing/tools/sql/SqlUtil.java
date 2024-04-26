package com.zhangfuxing.tools.sql;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/25
 * @email zhangfuxing1010@163.com
 */
public class SqlUtil {

    public static SqlBuilder delete() {
         return new SqlBuilder(DMLType.DELETE);
    }

    public static SqlBuilder select() {
        return new SqlBuilder(DMLType.SELECT);
    }

    public static SqlBuilder insert() {
        return new SqlBuilder(DMLType.INSERT);
    }

    public static SqlBuilder update() {
        return new SqlBuilder(DMLType.UPDATE);
    }
}
