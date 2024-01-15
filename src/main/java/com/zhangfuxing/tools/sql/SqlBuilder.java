package com.zhangfuxing.tools.sql;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/1/5
 * @email zhangfuxing@kingshine.com.cn
 */
public class SqlBuilder {
    private SQLType type;
    private SqlColumn selectColumn;
    private SqlWhere where;
    private SqlOrder order;
    private String from;

    public SqlBuilder setType(SQLType type) {
        this.type = type;
        return this;
    }

    public SqlBuilder setSelectColumn(SqlColumn selectColumn) {
        this.selectColumn = selectColumn;
        return this;
    }

    public SqlBuilder setWhere(SqlWhere where) {
        this.where = where;
        return this;
    }

    public SqlBuilder setOrder(SqlOrder order) {
        this.order = order;
        return this;
    }

    public SqlBuilder setFrom(String from) {
        this.from = from;
        return this;
    }

    public String build() {
        return switch (type) {
            case SELECT:
                yield buildSelect();
            case INSERT:
                yield buildInsert();
            case UPDATE:
                yield buildUpdate();
            case DELETE:
                yield buildDelete();
        };
    }

    public static SqlBuilder newInstance() {
        return new SqlBuilder();
    }

    private String buildSelect() {
        return "SELECT " + selectColumn.get() + " FROM " + from + appendWhere() + appendOrder();
    }

    private String buildInsert() {
        return "";
    }

    private String buildDelete() {
        return "DELETE FROM " + from + appendWhere();
    }

    private String buildUpdate() {
        return "UPDATE " + from + " SET " + appendSet() + appendWhere();
    }

    private String appendSet() {
        return "";
    }

    private String appendOrder() {
        if (order == null) {
            return "";
        }
        return " ORDER BY " + order.get();
    }

    private String appendWhere() {
        if (where == null) {
            return "";
        }
        return " WHERE " + where.get();
    }
}
