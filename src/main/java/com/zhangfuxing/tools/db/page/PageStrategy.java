package com.zhangfuxing.tools.db.page;

public interface PageStrategy {
    /**
     * 判断是否支持该数据库类型
     */
    boolean support(String dbType);

    /**
     * 构建分页SQL
     */
    String buildPageSql(String sql, int offset, int limit);
} 