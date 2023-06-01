package com.zhangfuxing.tools.test;

import com.zhangfuxing.tools.StringUtil;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2023/05/06
 * @email zhangfuxing1010@163.com
 */
public class Base64Test {
	public static void main(String[] args) {
		String format = StringUtil.format("{path}/{projectId}", "/usr/project/doc", 1,"aaa");
		System.out.println(format);
	}
	
}
