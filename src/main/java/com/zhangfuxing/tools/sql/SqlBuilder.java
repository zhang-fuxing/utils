package com.zhangfuxing.tools.sql;

import com.zhangfuxing.tools.common.enums.JoinStr;
import com.zhangfuxing.tools.util.DateUtil;
import com.zhangfuxing.tools.util.RefUtil;
import com.zhangfuxing.tools.util.Str;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/25
 * @email zhangfuxing1010@163.com
 */
public class SqlBuilder {
    private static final Str DELETE = new Str("delete from {0} where {1}");
    private static final Str SELECT = new Str("select {} from {} where {} {groupBy} {having} {orderBy}");
    private static final Str UPDATE = new Str("update {} set {} where {}");
    private static final Str INSERT = new Str("insert into {} ({}) values {}");


    DMLType dmlType;
    private String tableName;
    private String columns;
    private List<String> where = new ArrayList<>(1);
    private String groupBy;
    private String having;
    private String orderBy;
    private Map<String, Object> keyValue;
    // insert into 使用的values
    private final List<List<Object>> values = new ArrayList<>();

    public SqlBuilder() {
    }

    @SafeVarargs
    public final SqlBuilder values(List<Object>... valueList) {
        for (List<Object> values : valueList) {
            if (values == null) {
                continue;
            }
            this.values.add(values);
        }
        return this;
    }

    public SqlBuilder set(Map<String, Object> colAndValue) {
        this.keyValue = colAndValue;
        return this;
    }

    public SqlBuilder set(Object... keyAndValue) {
        int len = keyAndValue.length;
        if (len == 0 || len % 2 != 0) {
            throw new IllegalArgumentException("不能将每一个key都对应一个value，传递的参数应为2的倍数。");
        }
        Map<String, Object> setMap = new HashMap<>();
        for (int i = 0; i < keyAndValue.length; i += 2) {
            setMap.put(String.valueOf(keyAndValue[i]), keyAndValue[i + 1]);
        }
        if (keyValue == null) {
            keyValue = new HashMap<>();
        }
        keyValue.putAll(setMap);
        return this;
    }

    public SqlBuilder(DMLType dmlType) {
        this.dmlType = dmlType;
    }

    public SqlBuilder dml(DMLType dmlType) {
        this.dmlType = dmlType;
        return this;
    }

    public SqlBuilder groupBy(String... columns) {
        this.groupBy = Arrays.stream(columns)
                .filter(c -> !Str.isBlank(c))
                .collect(Collectors.joining(", "));
        return this;
    }

    public SqlBuilder having(Where... wheres) {
        SqlBuilder sqlBuilder = new SqlBuilder().dml(DMLType.NONE).whereAnd(wheres);
        this.having = sqlBuilder.buildWhere();
        return this;
    }

    public SqlBuilder orderBy(Order... orders) {
        orderBy = Arrays.stream(orders)
                .map(o -> o.col() + " " + o.orderType())
                .collect(Collectors.joining(", "));
        return this;
    }


    public SqlBuilder form(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public SqlBuilder columns(String... columns) {
        this.columns = Str.join(JoinStr.of(", "), columns);
        return this;
    }

    public SqlBuilder columns(Class<?> clazz) {
        Field[] fields = RefUtil.getAllFields(clazz);
        String[] array = Arrays.stream(fields).
                filter(f -> f.isAnnotationPresent(Col.class))
                .map(f -> {
                    Col annotation = f.getAnnotation(Col.class);
                    String value = annotation.value();
                    String fieldName = f.getName();
                    return Str.isBlank(value) ? fieldName : value;
                })
                .toArray(String[]::new);
        return columns(array);
    }

    public SqlBuilder where(String col, Object value) {
        return where(col, CondMode.EQ, value);
    }

    public SqlBuilder where(String col, CondMode condMode, Object value) {
        String val;
        if (value instanceof String str) {
            val = "'" + str + "'";
        } else {
            val = Str.valueOf(value);
        }
        switch (condMode) {
            case EQ -> where.add(col + " = " + val);
            case NE -> where.add(col + " <> " + val);
            case LT -> where.add(col + " < " + val);
            case LE -> where.add(col + " <= " + val);
            case GT -> where.add(col + " > " + val);
            case GE -> where.add(col + " >= " + val);
            case IN -> in(col, value);
            case LIKE -> where.add(col + " like '%" + value + "%'");
            case IS_NULL -> where.add((col + " is null"));
            case IS_NOT_NULL -> where.add(col + " is not null");
        }
        return this;
    }


    public SqlBuilder where(Where where) {
        return where(where.col(), where.condMode(), where.value());
    }

    public SqlBuilder whereOr(Where... wheres) {
        return where(wheres, " or ");
    }

    public SqlBuilder whereAnd(Where... wheres) {
        return where(wheres, " and ");
    }

    private SqlBuilder where(Where[] wheres, String delimiter) {
        SqlBuilder sqlBuilder = new SqlBuilder(DMLType.NONE);
        for (Where w : wheres) {
            sqlBuilder.where(w.col(), w.condMode(), w.value());
        }

        if (!sqlBuilder.where.isEmpty()) {
            StringJoiner stringJoiner = new StringJoiner(delimiter, "(", ")");
            for (String s : sqlBuilder.where) {
                stringJoiner.add(s);
            }
            this.where.add(stringJoiner.toString());
        }
        return this;
    }

    private void in(String col, Object value) {
        if (value == null) return;
        if (value instanceof Collection<?> collection) {
            if (collection.isEmpty()) {
                return;
            }
            StringJoiner stringJoiner = new StringJoiner(", ", "(", ")");
            for (Object next : collection) {
                if (next == null) {
                    continue;
                }
                stringJoiner.add(sqlEncoding(next));
            }
            this.where.add(col + " in" + stringJoiner);
        }

    }

    private String sqlEncoding(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String v) {
            return "'" + v + "'";
        }
        if (value instanceof Byte[] b) {
            return Str.byteToHex(b);
        }
        if (value instanceof byte[] b) {
            return Str.byteToHex(b);
        }
        if (value instanceof Date date) {
            return DateUtil.format(date);
        }
        return String.valueOf(value);
    }


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public List<String> getWhere() {
        return where;
    }

    public void setWhere(List<String> where) {
        this.where = where;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getHaving() {
        return having;
    }

    public void setHaving(String having) {
        this.having = having;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String build() {
        return switch (this.dmlType) {
            case INSERT -> insert();
            case DELETE -> delete();
            case UPDATE -> update();
            case SELECT -> select();
            case NONE -> "";
        };
    }


    private String select() {
        if (!Str.isBlank(groupBy)) {
            groupBy = " group by " + groupBy;
        }
        if (!Str.isBlank(having)) {
            having = " having " + having;
        }
        if (!Str.isBlank(orderBy)) {
            this.orderBy = " order by " + orderBy;
        }
        return SELECT.instanceFmt(columns, tableName, buildWhere(), groupBy, having, orderBy);
    }

    private String buildWhere() {
        if (where == null) {
            return "1=1";
        }
        return String.join(" and ", where);
    }

    private String insert() {
        if (Str.isBlank(columns)) {
            throw new IllegalArgumentException("要插入的列不能为空");
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException("要插入的值不能为空");
        }
        for (List<Object> list : values) {
            if (list.size() != columns.split(",").length) {
                throw new IllegalArgumentException("要插入的value列表与要插入的列数量不一致");
            }
        }

        String valueStr = values.stream()
                .map(value -> value.stream().map(this::sqlEncoding).collect(Collectors.joining(",", "(", ")")))
                .collect(Collectors.joining(","));

        return INSERT.instanceFmt(tableName, columns, valueStr);
    }

    private String update() {
        if (this.keyValue == null) {
            throw new IllegalArgumentException("不能更新空的key 和 value");
        }

        StringJoiner stringJoiner = new StringJoiner(", ");
        for (var entry : keyValue.entrySet()) {
            String key = entry.getKey();
            String value = sqlEncoding(entry.getValue());
            stringJoiner.add(key + "=" + value);
        }
        return UPDATE.instanceFmt(tableName, stringJoiner, buildWhere());
    }

    private String delete() {
        return DELETE.instanceFmt(tableName, buildWhere());
    }

}
