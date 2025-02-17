package com.zhangfuxing.tools.handler.example;

import com.zhangfuxing.tools.handler.AutoHandler;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/1/6
 * @email zhangfuxing1010@163.com
 */
@AutoHandler
public class SqlServerPageHandler implements PagedHandler {
	PagedHandler nextHandler;


	@Override
	public boolean support(DbType dbType) {
		return dbType == DbType.SQLServer;
	}

	@Override
	public String addPages(String sql, int offset, int limit) {
		return sql + " ORDER BY (SELECT NULL) OFFSET " + offset +
			   " ROWS FETCH NEXT " + limit + " ROWS ONLY";
	}


	@Override
	public void setNext(PagedHandler next) {
		this.nextHandler = next;
	}

	@Override
	public PagedHandler getNext() {
		return this.nextHandler;
	}
}
