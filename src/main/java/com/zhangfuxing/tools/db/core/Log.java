package com.zhangfuxing.tools.db.core;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/2
 * @email zhangfuxing1010@163.com
 */
public class Log {

    public static void info(String msg, Object... args) {
        System.out.printf(msg + "%n", args);
    }

    public static void warn(String msg, Object... args) {
        System.out.printf(msg + "%n", args);
    }

    public static void error(String msg,Throwable throwable, Object... args) {
        System.err.printf(msg + "%n", args);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

}
