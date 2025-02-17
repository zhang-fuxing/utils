package com.zhangfuxing.tools.handler.example;

import com.zhangfuxing.tools.handler.AutoHandler;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/1/6
 * @email zhangfuxing1010@163.com
 */
@AutoHandler
public class KingbasePageHandler implements PagedHandler {

	PagedHandler nextHandler;

	@Override
	public boolean support(DbType dbType) {
		return dbType == DbType.Kingbase;
	}

	@Override
	public String addPages(String sql, int offset, int limit) {
		return sql + " LIMIT " + limit + " OFFSET " + offset;
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
