package com.zhangfuxing.tools.handler.example;

import com.zhangfuxing.tools.handler.AutoHandler;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/1/6
 * @email zhangfuxing1010@163.com
 */
@AutoHandler
public class OraclePageHandler implements PagedHandler {

	PagedHandler nextHandler;

	@Override
	public boolean support(DbType dbType) {
		return dbType == DbType.Oracle;
	}

	@Override
	public String addPages(String sql, int offset, int limit) {
		return "SELECT * FROM (SELECT a.*, ROWNUM rn FROM (" + sql +
			   ") a WHERE ROWNUM <= " + (offset + limit) +
			   ") WHERE rn > " + offset;
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
