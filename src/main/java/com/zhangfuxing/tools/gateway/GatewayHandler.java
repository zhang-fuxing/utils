package com.zhangfuxing.tools.gateway;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/7/28
 * @email zhangfuxing1010@163.com
 */
public class GatewayHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private static final Logger logger = LoggerFactory.getLogger(GatewayHandler.class);
	private final DynamicConfigManager configManager;

	public GatewayHandler(DynamicConfigManager configManager) {
		this.configManager = configManager;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
		// 处理 CORS 预检请求
		if (request.method() == HttpMethod.OPTIONS) {
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE, "3600");
			response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
			return;
		}

		String path = request.uri();
		RouteConfig route = configManager.getRouteForPath(path);

		if (route == null) {
			logger.info("未找到相关的路由配置，无法代理目标：{}", request.uri());
			sendError(ctx, HttpResponseStatus.NOT_FOUND, "No route found for path: " + path);
			return;
		}

		try {
			// 构建转发URL
			String targetUrl = buildTargetUrl(route, request);

			// 创建转发请求
			FullHttpRequest proxyRequest = createProxyRequest(request, targetUrl);

			// 应用路由过滤器
			applyFilters(route, proxyRequest);

			// 执行转发
			executeProxyRequest(ctx, proxyRequest, route.getTarget());
		} catch (Exception e) {
			logger.error("Error processing request", e);
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}

	private String buildTargetUrl(RouteConfig route, FullHttpRequest request) {
		String result = route.routingUrl(request);
		logger.info("proxy: {}  ==>  {}", String.format("%s %s://%s%s", request.method().name(),
				request.protocolVersion().protocolName().toLowerCase(),
				request.headers().get("host"),
				request.uri()), result);
		return result;
	}

	private FullHttpRequest createProxyRequest(FullHttpRequest original, String targetUrl) {
		// 创建新的请求对象
		FullHttpRequest newRequest = new DefaultFullHttpRequest(
				original.protocolVersion(),
				original.method(),
				targetUrl,
				original.content().retainedDuplicate()
		);

		// 复制请求头
		newRequest.headers().setAll(original.headers());

		// 移除一些不应该转发的头部
		newRequest.headers().remove(HttpHeaderNames.CONNECTION);
		newRequest.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);
		// 更新Host头
		try {
			URI uri = new URI(targetUrl);
			newRequest.headers().set(HttpHeaderNames.HOST, uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : ""));
		} catch (URISyntaxException e) {
			logger.warn("Invalid target URL: {}", targetUrl);
		}

		return newRequest;
	}

	private void applyFilters(RouteConfig route, FullHttpRequest request) {
		// 应用配置的过滤器
		for (Map.Entry<String, String> filter : route.getFilters().entrySet()) {
			String filterName = filter.getKey();
			String filterConfig = filter.getValue();

			// 实现各种过滤器
			switch (filterName) {
				case "AddRequestHeader" -> {
					String[] parts = filterConfig.split("=", 2);
					if (parts.length == 2) {
						request.headers().add(parts[0], parts[1]);
					}
				}
				case "SetHostHeader" -> request.headers().set(HttpHeaderNames.HOST, filterConfig);
				case "StripPrefix" -> {
					int prefixCount = Integer.parseInt(filterConfig);
					String uri = request.uri();
					for (int i = 0; i < prefixCount; i++) {
						int nextSlash = uri.indexOf('/', 1);
						if (nextSlash > 0) {
							uri = uri.substring(nextSlash);
						} else {
							uri = "/";
							break;
						}
					}
					request.setUri(uri);
				}
			}
		}
	}

	private void executeProxyRequest(ChannelHandlerContext ctx, FullHttpRequest request, String targetUri) {
		// 解析目标主机和端口
		URI uri;
		try {
			uri = URI.create(targetUri);
		} catch (Exception e) {
			sendError(ctx, HttpResponseStatus.BAD_GATEWAY, "Invalid target URI: " + targetUri);
			return;
		}

		String host = uri.getHost();
		int port = uri.getPort() > 0 ? uri.getPort() :
				"https".equals(uri.getScheme()) ? 443 : 80;

		// 使用Bootstrap连接到目标服务器而不是HttpClient
		// 创建Bootstrap来建立到目标服务器的连接
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(ctx.channel().eventLoop())
				.channel(ctx.channel().getClass())
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
				.handler(new ChannelInitializer<>() {
					@Override
					protected void initChannel(Channel ch) {
						ch.pipeline()
								.addLast(new HttpClientCodec())
								.addLast(new HttpObjectAggregator(1024 * 1024))
								.addLast(new HttpResponseHandler(ctx));
					}
				});

		bootstrap.connect(host, port)
				.addListener((ChannelFuture future) -> {
					if (future.isSuccess()) {
						Channel proxyChannel = future.channel();
						proxyChannel.writeAndFlush(request)
								.addListener((ChannelFuture writeFuture) -> {
									if (!writeFuture.isSuccess()) {
										closeOnFlush(proxyChannel);
										sendError(ctx, HttpResponseStatus.BAD_GATEWAY, "Proxy connection failed");
									}
								});
					} else {
						logger.error("Failed to connect to target service: " + host + ":" + port, future.cause());
						sendError(ctx, HttpResponseStatus.BAD_GATEWAY, "Cannot connect to target service");
					}
				});
	}

	private static class HttpResponseHandler extends ChannelInboundHandlerAdapter {
		private final ChannelHandlerContext clientContext;

		public HttpResponseHandler(ChannelHandlerContext clientContext) {
			this.clientContext = clientContext;
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			if (msg instanceof FullHttpResponse response) {
				// 添加 CORS 头部
				response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
				response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
				response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");

				// 将响应写回客户端，使用retainedDuplicate确保引用计数正确
				clientContext.writeAndFlush(response.retainedDuplicate())
						.addListener(ChannelFutureListener.CLOSE);
			} else {
				// 释放不处理的消息
				ReferenceCountUtil.release(msg);
			}
			closeOnFlush(ctx.channel());
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			logger.error("Proxy response error", cause);
			closeOnFlush(ctx.channel());
		}
	}

	private static void closeOnFlush(Channel ch) {
		if (ch.isActive()) {
			ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}

	private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String message) {
		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, status,
				Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

		// 添加 CORS 头部到错误响应
		response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
		response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");

		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error("Gateway error", cause);
		sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Gateway internal error");
		closeOnFlush(ctx.channel());
	}
}
