package com.zhangfuxing.tools.db;

import com.zhangfuxing.tools.classutil.ClassUtil;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/25
 * @email zhangfuxing@kingshine.com.cn
 */
public class Cols {
    private String columnName;
    private Object columnValue;
    private Class<?> columnType;

    public Cols() {
    }

    public Cols(String columnName, Object columnValue, Class<?> columnType) {
        this.columnName = columnName;
        this.columnValue = columnValue;
        this.columnType = columnType;
    }

    public Cols(String columnName, Object columnValue, String columnType) {
        this.columnName = columnName;
        this.columnValue = columnValue;
        try {
            this.columnType = ClassUtil.loadClass(columnType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Object getColumnValue() {
        return columnValue;
    }

    public void setColumnValue(Object columnValue) {
        this.columnValue = columnValue;
    }

    public Class<?> getColumnType() {
        return columnType;
    }

    public void setColumnType(Class<?> columnType) {
        this.columnType = columnType;
    }

    @Override
    public String toString() {
        return "Col{" +
               "columnName='" + columnName + '\'' +
               ", columnValue=" + columnValue +
               ", columnType=" + columnType +
               '}';
    }
}
