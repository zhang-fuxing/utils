package com.zhangfuxing.tools;

import com.zhangfuxing.tools.sql.CondMode;
import com.zhangfuxing.tools.sql.Order;
import com.zhangfuxing.tools.sql.SqlUtil;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/25
 * @email zhangfuxing1010@163.com
 */
public class MainTest {

    public static void main(String[] args) {
        var values = SqlUtil.select()
                .columns("UserID", "UserName", "Password", "IsInvalid")
                .form("UserInfo")
                .where("UserName", CondMode.LIKE, "z")
                .orderBy(Order.ofAsc("1"))
                .build();
        System.out.println(values);
    }

}
