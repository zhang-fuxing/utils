package com.zhangfuxing.tools.gateway;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/7/29
 * @email zhangfuxing1010@163.com
 */
public class GatewayMain {
	public static void main(String[] args) {
		DynamicGateway gateway = GatewayApplication.run(32000,
				new RouteConfig()
						.setSource("^/api/(.*)")
						.setTarget("http://localhost:9999/api/$1"),
				new RouteConfig()
						.setSource("^/onlyoffice/(.*)")
						.setTarget("http://192.168.235.58:20080/$1"),
				new RouteConfig()
						.setSource("/")
						.setRoot("C:\\Users\\kingshine\\Desktop\\web\\82")
						.setIndex("index.html"));
	}
}
