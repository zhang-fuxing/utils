package com.zhangfuxing.tools;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2023/04/25
 * @email zhangfuxing1010@163.com
 */
public class SQLDateUtil {
	
	public static Date now() {
		return new Date(System.currentTimeMillis());
	}
	
	public static Timestamp time() {
		return new Timestamp(System.currentTimeMillis());
	}
	
	public static Timestamp convert(java.util.Date date) {
		return new Timestamp(date.getTime());
	}
}
