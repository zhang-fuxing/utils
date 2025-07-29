package com.zhangfuxing.tools.gateway;

/**
 * 网关应用启动类
 *
 * @author 张福兴
 * @version 1.0
 * @date 2025/7/29
 * @email zhangfuxing1010@163.com
 */
public class GatewayApplication {

	public static DynamicGateway run(int listenPort,RouteConfig... config) {
		DynamicGateway gateway = new DynamicGateway(new DynamicConfigManager(listenPort,  config));
		try {
			gateway.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return gateway;
	}
}
