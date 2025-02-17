package com.zhangfuxing.tools.handler.example;

import com.zhangfuxing.tools.handler.AutoHandlerSupport;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/1/6
 * @email zhangfuxing1010@163.com
 */
public interface PagedHandler extends AutoHandlerSupport<PagedHandler> {

	boolean support(DbType dbType);

	String addPages(String sql, int offset, int limit);

}
