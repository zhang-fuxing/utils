package com.zhangfuxing.tools.db;

import java.util.List;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/25
 * @email zhangfuxing@kingshine.com.cn
 */
public class Rows {
    List<Cols> columns;

    public Rows() {
    }

    public Rows(List<Cols> columns) {
        this.columns = columns;
    }

    public List<Cols> getColumns() {
        return columns;
    }

    public void setColumns(List<Cols> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "Rows{" +
               "columns=" + columns +
               '}';
    }
}
