package com.zhangfuxing.tools.sql;

import com.zhangfuxing.tools.util.Str;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/1/5
 * @email zhangfuxing@kingshine.com.cn
 */
public class SqlOrder {
    private String orderBy;

    public SqlOrder() {
        this.orderBy = "";
    }

    public static SqlOrder newInstance() {
        return new SqlOrder();
    }

    public SqlOrder add(String orderBy) {
        return add(orderBy, OrderBy.ASC);
    }

    public SqlOrder add(String column, OrderBy by) {
//        this.orderBy = Str.fmt("{}, {} {}", this.orderBy, column, by);
        this.orderBy = Str.joiner(",", this.orderBy, Str.fmt("{} {}", column, by));
        return this;
    }


    public String get() {
        return this.orderBy;
    }
}
