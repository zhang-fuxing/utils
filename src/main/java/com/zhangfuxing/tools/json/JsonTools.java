package com.zhangfuxing.tools.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/4/18
 * @email zhangfuxing1010@163.com
 */
public class JsonTools {

	public static JsonObj createObj() {
		return JsonObj.create();
	}

	public static JsonArr createArr() {
		return JsonArr.create();
	}

	public static JsonObj parseObj(Object obj) {
		return JsonObj.parse(obj);
	}

	public static JsonArr parseArr(Object obj) {
		return JsonArr.parse(obj);
	}

	public static JsonObj createObj(Consumer<ObjectMapper> consumer) {
		return JsonObj.create().config(consumer);
	}

	public static JsonArr createArr(Consumer<ObjectMapper> consumer) {
		return JsonArr.create().config(consumer);
	}


}
