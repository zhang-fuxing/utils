package com.zhangfuxing.tools.sql;

import com.zhangfuxing.tools.util.Str;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/1/5
 * @email zhangfuxing@kingshine.com.cn
 */
public class SqlColumn {
    private String column;

    public SqlColumn() {
        column = "";
    }

    public static SqlColumn newSqlColumn() {
        return new SqlColumn();
    }

    public SqlColumn add(String column, String alias) {
        String joiner = Str.joiner(" ", column, alias);
        this.column = Str.joiner(",", this.column, joiner);
        return this;
    }
    public SqlColumn add(String column) {
        String joiner = Str.joiner(" ", column, null);
        this.column = Str.joiner(", ",this. column, joiner);
        return this;
    }
    public String get() {
        return column;
    }
}
