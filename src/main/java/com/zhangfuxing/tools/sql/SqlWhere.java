package com.zhangfuxing.tools.sql;

import com.zhangfuxing.tools.util.Str;

import java.util.StringJoiner;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/1/5
 * @email zhangfuxing@kingshine.com.cn
 */
public class SqlWhere {
    String where;

    public SqlWhere() {
        this.where = "";
    }

    public static SqlWhere newInstance() {
        return new SqlWhere();
    }

    public SqlWhere leftBracket() {
        this.where += " (";
        return this;
    }

    public SqlWhere rightBracket() {
        this.where += " )";
        return this;
    }

    public SqlWhere and() {
        this.where += " AND ";
        return this;
    }

    public SqlWhere or() {
        this.where += " OR ";
        return this;
    }

    public SqlWhere eq(String column, Object value) {
        this.where = Str.fmtSqlVal("{0} {1}={2}", Str.newInstance(this.where), Str.newInstance(column), value);
        return this;
    }

    public SqlWhere ne(String column, Object value) {
        this.where = Str.fmtSqlVal("{0} {1}<>{2}", Str.newInstance(this.where), Str.newInstance(column), value);
        return this;
    }

    public SqlWhere gt(String column, Object value) {
        this.where = Str.fmtSqlVal("{0} {1}>{2}", Str.newInstance(this.where), Str.newInstance(column), value);
        return this;
    }
    public SqlWhere ge(String column, Object value) {
        this.where = Str.fmtSqlVal("{0} {1}>={2}", Str.newInstance(this.where), Str.newInstance(column), value);
        return this;
    }
    public SqlWhere lt(String column, Object value) {
        this.where = Str.fmtSqlVal("{0} {1}<{2}", Str.newInstance(this.where), Str.newInstance(column), value);
        return this;
    }
    public SqlWhere le(String column, Object value) {
        this.where = Str.fmtSqlVal("{0} {1}<={2}", Str.newInstance(this.where), Str.newInstance(column), value);
        return this;
    }
    public SqlWhere like(String column, Object value) {
        this.where = Str.fmtSqlVal("{0} {1} LIKE '{2}'", Str.newInstance(this.where), Str.newInstance(column), new Str(Str.valueOf(value)));
        return this;
    }
    public SqlWhere likeAny(String column, Object value) {
        this.where = Str.fmtSqlVal("{0} {1} LIKE '%{2}%'", Str.newInstance(this.where), Str.newInstance(column), new Str(Str.valueOf(value)));
        return this;
    }

    public <T> SqlWhere in(String column, Iterable<T> iterable) {
        if (iterable == null || !iterable.iterator().hasNext()) {
            return this;
        }
        StringJoiner joiner = getIn(iterable);

        this.where = Str.fmtSqlVal("{0} {1} IN ({2})", Str.newInstance(this.where), Str.newInstance(column), Str.newInstance(joiner.toString()));
        return this;
    }

    public <T> SqlWhere notIn(String column, Iterable<T> iterable) {
        if (iterable == null ||!iterable.iterator().hasNext()) {
            return this;
        }
        StringJoiner joiner = getIn(iterable);

        this.where = Str.fmtSqlVal("{0} {1} NOT IN ({2})", Str.newInstance(this.where), Str.newInstance(column), Str.newInstance(joiner.toString()));
        return this;
    }

    public SqlWhere isNull(String column) {
        this.where = Str.fmtSqlVal("{0} {1} IS NULL", Str.newInstance(this.where), Str.newInstance(column));
        return this;
    }

    public SqlWhere isNotNull(String column) {
        this.where = Str.fmtSqlVal("{0} {1} IS NOT NULL", Str.newInstance(this.where), Str.newInstance(column));
        return this;
    }

    private <T> StringJoiner getIn(Iterable<T> iterable) {
        StringJoiner joiner = new StringJoiner(",");
        iterable.forEach(item -> {
            if (item instanceof String val) {
                val = "'" + val + "'";
                joiner.add(val);
            } else {
                joiner.add(Str.valueOf(item));
            }
        });
        return joiner;
    }

    public String get() {
        return where;
    }
}
