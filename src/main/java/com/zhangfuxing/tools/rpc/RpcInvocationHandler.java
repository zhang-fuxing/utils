package com.zhangfuxing.tools.rpc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ServiceLoaderUtil;
import cn.hutool.json.JSONUtil;
import com.zhangfuxing.tools.http.HttpClientUtil;
import com.zhangfuxing.tools.http.HttpRequestBuilder;
import com.zhangfuxing.tools.rpc.anno.*;
import com.zhangfuxing.tools.rpc.convert.ResponseConverter;
import com.zhangfuxing.tools.rpc.error.DefaultErrorHandler;
import com.zhangfuxing.tools.rpc.error.ErrorHandler;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.*;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/17
 * @email zhangfuxing1010@163.com
 */
public class RpcInvocationHandler implements InvocationHandler {
	private String basURL;
	private int maxRetries = 3; // 默认重试次数
	private ErrorHandler errorHandler;

	List<RpcRequestProcessor> rpcProcessors;

	{
		rpcProcessors = ServiceLoaderUtil.loadList(RpcRequestProcessor.class);
		errorHandler = new DefaultErrorHandler();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String targetUrl = null;
		try {
			// 1. 获取注解和基本信息
			RpcRequestInfo requestInfo = extractRequestInfo(method);

			// 2. 处理请求参数
			RequestParameters parameters = processParameters(method, args);

			// 3. 构建URL
			targetUrl = buildTargetUrl(requestInfo, parameters);

			// 4. 构建请求
			var builder = buildRequestBuilder(targetUrl, requestInfo, parameters);

			// 5. 执行请求并处理响应
			return executeRequest(builder, method, requestInfo);
		} catch (Exception e) {
			throw errorHandler.wrapException(e, targetUrl, method);
		}
	}

	private record RpcRequestInfo(RpcClient rpcClient, RpcMapping rpcMapping, String baseUri) {
	}

	private static class RequestParameters {
		final StringJoiner urlParams = new StringJoiner("&");
		final Map<String, String> pathVariables = new HashMap<>();
		HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();
		Map<String, String> cookieHead;
		RpcHeader rpcHeaders;
	}

	private RpcRequestInfo extractRequestInfo(Method method) {
		Class<?> declaringClass = method.getDeclaringClass();
		RpcClient rpcClient = declaringClass.getAnnotation(RpcClient.class);
		String baseUri = Optional.ofNullable(declaringClass.getAnnotation(RpcMapping.class))
				.map(RpcMapping::value)
				.orElse("");
		RpcMapping rpcMapping = getAnnotation(method, RpcMapping.class);
		return new RpcRequestInfo(rpcClient, rpcMapping, baseUri);
	}

	private RequestParameters processParameters(Method method, Object[] args) {
		RequestParameters params = new RequestParameters();
		if (args == null || method.getParameters().length == 0) {
			return params;
		}

		for (int i = 0; i < method.getParameters().length; i++) {
			processParameter(method.getParameters()[i], args[i], params);
		}
		return params;
	}

	private void processParameter(Parameter parameter, Object arg, RequestParameters params) {
		RpcParam annotation = parameter.getAnnotation(RpcParam.class);
		if (annotation != null) {
			processUrlParameter(annotation, arg, params);
			return;
		}

		RpcPathVariable pathVariable = parameter.getAnnotation(RpcPathVariable.class);
		if (pathVariable != null) {
			processPathVariable(pathVariable, arg, params);
			return;
		}

		RpcBody rpcBody = parameter.getAnnotation(RpcBody.class);
		if (rpcBody != null) {
			processBodyParameter(rpcBody, arg, params);
			return;
		}

		if (arg instanceof RpcCookie cookies) {
			params.cookieHead = cookies.getCookies();
		} else if (arg instanceof RpcHeader rpcHeader) {
			params.rpcHeaders = rpcHeader;
		}
	}

	private void processUrlParameter(RpcParam annotation, Object arg, RequestParameters params) {
		String paraName = annotation.value();
		if (arg == null && annotation.required()) {
			throw new IllegalArgumentException(
					String.format("参数：%s 是必须的，如果不需要此参数，请设置 @RpcParam(required=false)", paraName));
		}
		if (arg != null) {
			params.urlParams.add(paraName + "=" + URLEncoder.encode(String.valueOf(arg), StandardCharsets.UTF_8));
		}
	}

	private void processPathVariable(RpcPathVariable annotation, Object arg, RequestParameters params) {
		String paraName = annotation.value();
		if (arg == null && annotation.required()) {
			throw new IllegalArgumentException(
					String.format("路径参数：%s 是必须的", paraName));
		}
		if (arg != null) {
			params.pathVariables.put(paraName, URLEncoder.encode(String.valueOf(arg), StandardCharsets.UTF_8));
		}
	}

	private void processBodyParameter(RpcBody rpcBody, Object arg, RequestParameters params) {
		if (arg == null) {
			return;
		}

		switch (rpcBody.value()) {
			case JSON -> params.bodyPublisher = HttpRequest.BodyPublishers.ofString(
					JSONUtil.toJsonStr(arg), Charset.forName(rpcBody.charset()));
			case INPUT_STREAM -> params.bodyPublisher = createInputStreamPublisher(arg);
			case TEXT -> processTextBody(arg, rpcBody.charset(), params);
			case NONE -> params.bodyPublisher = HttpRequest.BodyPublishers.noBody();
		}
	}

	private HttpRequest.BodyPublisher createInputStreamPublisher(Object arg) {
		if (arg instanceof InputStream inputStream) {
			return HttpRequest.BodyPublishers.ofInputStream(() -> {
				Thread currentThread = Thread.currentThread();
				if (currentThread.isInterrupted()) {
					try {
						inputStream.close();
					} catch (Exception ignored) {
						// 忽略关闭时的异常
					}
					throw new RuntimeException("请求被中断");
				}
				return inputStream;
			});
		}
		throw new IllegalArgumentException("请求体参数不是InputStream对象");
	}

	private void processTextBody(Object arg, String charset, RequestParameters params) {
		Charset charsetObj = Charset.forName(charset);
		if (arg instanceof String str) {
			params.bodyPublisher = HttpRequest.BodyPublishers.ofString(str, charsetObj);
		} else if (arg instanceof Map<?, ?> map) {
			StringJoiner joiner = new StringJoiner("&");
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				joiner.add(entry.getKey() + "=" + URLEncoder.encode(String.valueOf(entry.getValue()), charsetObj));
			}
			params.bodyPublisher = HttpRequest.BodyPublishers.ofString(joiner.toString(), charsetObj);
		} else {
			params.bodyPublisher = HttpRequest.BodyPublishers.ofString(String.valueOf(arg), charsetObj);
		}
	}

	private HttpResponse.BodyHandler<?> selectBodyHandler(Method method) {
		Class<?> returnType = method.getReturnType();
		Type type = method.getGenericReturnType();
		if (InputStream.class.isAssignableFrom(returnType)) {
			return HttpResponse.BodyHandlers.ofInputStream();
		}
		// String
		else if (String.class.equals(returnType)) {
			return HttpResponse.BodyHandlers.ofString();
		}
		// byte
		else if (byte[].class.equals(returnType)) {
			return HttpResponse.BodyHandlers.ofByteArray();
		}
		// ref
		else if (returnType.isAssignableFrom(Ref.class) && type instanceof ParameterizedType pt) {
			Type actualType = pt.getActualTypeArguments()[0];
			if (InputStream.class.getTypeName().equals(actualType.getTypeName())) {
				return HttpResponse.BodyHandlers.ofInputStream();
			} else if (String.class.getTypeName().equals(actualType.getTypeName())) {
				return HttpResponse.BodyHandlers.ofString();
			} else if (byte[].class.getTypeName().equals(actualType.getTypeName())) {
				return HttpResponse.BodyHandlers.ofByteArray();
			} else {
				return HttpResponse.BodyHandlers.ofString();
			}
		}
		// default
		else {
			// 默认使用String处理器，后续通过ResponseConverter转换
			return HttpResponse.BodyHandlers.ofString();
		}
	}

	private String buildTargetUrl(RpcRequestInfo requestInfo, RequestParameters parameters) {
		String uri = requestInfo.baseUri + requestInfo.rpcMapping.value();

		// 处理路径变量
		for (Map.Entry<String, String> entry : parameters.pathVariables.entrySet()) {
			uri = uri.replace("{" + entry.getKey() + "}", entry.getValue());
		}

		// 检查是否还有未处理的路径变量
		if (uri.matches(".*\\{[^}]+\\}.*")) {
			throw new IllegalArgumentException("存在未处理的路径变量：" + uri);
		}

		if (!parameters.urlParams.toString().isBlank()) {
			uri = uri + "?" + parameters.urlParams;
		}
		return normalizeUrl(this.basURL, uri);
	}

	private HttpRequestBuilder buildRequestBuilder(
			String targetUrl, RpcRequestInfo requestInfo, RequestParameters parameters) {
		var builder = HttpClientUtil.client()
				.request()
				.url(targetUrl)
				.method(requestInfo.rpcMapping.method().name(), parameters.bodyPublisher);

		// 设置超时
		configureTimeout(builder, requestInfo);

		// 添加头部信息
		addHeaders(requestInfo.rpcMapping.headers(), builder);
		addHeaders(requestInfo.rpcClient.headers(), builder);
		addCookieHeaders(parameters.cookieHead, builder);
		if (parameters.rpcHeaders != null) {
			parameters.rpcHeaders.getHeader().forEach(builder::header);
		}

		// 处理请求处理器
		processRequestProcessors(builder);

		return builder;
	}

	private Object executeRequest(HttpRequestBuilder builder, Method method, RpcRequestInfo requestInfo) throws Exception {
		// 处理void返回类型
		if (method.getReturnType() == Void.TYPE) {
			builder.response(HttpResponse.BodyHandlers.discarding()).statusCode();
			return null;
		}

		// 选择合适的BodyHandler
		HttpResponse.BodyHandler<?> bodyHandler = selectBodyHandler(method);

		// 执行请求
		int retries = determineRetries(requestInfo.rpcClient);
		HttpResponse<?> response = executeWithRetry(builder, bodyHandler, retries);

		// 转换响应
		return ResponseConverter.convert(response, method);
	}

	private void configureTimeout(HttpRequestBuilder builder, RpcRequestInfo requestInfo) {
		long timeout = requestInfo.rpcMapping.timeout();
		if (timeout <= 0) {
			timeout = requestInfo.rpcClient.timeout();
		}
		if (timeout > 0) {
			builder.timeout(Duration.ofMillis(timeout));
		}
	}

	private void addCookieHeaders(Map<String, String> cookieHead, HttpRequestBuilder builder) {
		if (cookieHead != null && !cookieHead.isEmpty()) {
			StringJoiner joiner = new StringJoiner("; ");
			for (Map.Entry<String, String> entry : cookieHead.entrySet()) {
				joiner.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
			}
			builder.header(RpcCookie.COOKIE, joiner.toString());
		}
	}

	private void processRequestProcessors(HttpRequestBuilder builder) {
		if (!CollUtil.isEmpty(this.rpcProcessors)) {
			for (var rpcProcessor : this.rpcProcessors) {
				if (rpcProcessor != null) {
					rpcProcessor.processor(builder);
				}
			}
		}
	}

	private int determineRetries(RpcClient rpcClient) {
		int retries = this.maxRetries;
		if (retries <= 0) {
			retries = rpcClient.maxRetries();
		}
		return retries;
	}

	private HttpResponse<?> executeWithRetry(
			HttpRequestBuilder builder, HttpResponse.BodyHandler<?> bodyHandler, int retries) throws Exception {
		if (retries <= 0) {
			return builder.response(bodyHandler);
		}
		return retryableInvoke(builder, bodyHandler, retries);
	}

	private HttpResponse<?> retryableInvoke(HttpRequestBuilder builder, HttpResponse.BodyHandler<?> bodyHandler, int maxRetries) throws Exception {
		int retryCount = 0;
		Exception lastException = null;
		Thread currentThread = Thread.currentThread();

		while (retryCount < maxRetries) {
			try {
				if (currentThread.isInterrupted()) {
					throw new InterruptedException("请求被中断");
				}
				return builder.response(bodyHandler);
			} catch (Exception e) {
				lastException = e;
				// 检查是否应该重试
				if (!errorHandler.shouldRetry(e)) {
					throw new RuntimeException("不可重试的错误", e);
				}
				retryCount++;
				if (retryCount >= maxRetries) {
					break;
				}
				try {
					Thread.sleep(1000L * retryCount);
				} catch (InterruptedException ie) {
					currentThread.interrupt();
					throw new InterruptedException("重试等待被中断");
				}
			}
		}
		if (currentThread.isInterrupted()) {
			throw new InterruptedException("请求被中断");
		}
		throw new RuntimeException("调用失败，重试" + maxRetries + "次后仍然失败", lastException);
	}

	private static void addHeaders(String[] headers, HttpRequestBuilder builder) {
		if (headers == null) {
			return;
		}
		for (String header : headers) {
			if (header != null && contains(header, ":", "=")) {
				String[] split = header.split("[:=]");
				builder.header(split[0].trim(), split[1].trim());
			}
		}
	}

	private static boolean contains(String origin, String... chars) {
		for (String item : chars) {
			if (origin.contains(item)) {
				return true;
			}
		}
		return false;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public void setBasURL(String basURL) {
		this.basURL = basURL;
	}

	private String normalizeUrl(String baseUrl, String uri) {
		if (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		return baseUrl + uri;
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}


	public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
		T annotation = method.getAnnotation(annotationClass);
		if (annotation != null) {
			return annotation;
		}
		for (Annotation anno : method.getAnnotations()) {
			T metaAnnotation = anno.annotationType().getAnnotation(annotationClass);
			if (metaAnnotation != null) {
				return buildSynthesizedAnnotationImpl(anno, metaAnnotation);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Annotation> T buildSynthesizedAnnotationImpl(Annotation childAnnotation, T metaAnnotation) {
		return (T) Proxy.newProxyInstance(
				childAnnotation.annotationType().getClassLoader(),
				new Class<?>[]{metaAnnotation.annotationType()},
				new AnnotationInvocationHandler<>(childAnnotation, metaAnnotation));
	}

	// 辅助处理类（推荐封装为内部类）
	private record AnnotationInvocationHandler<A extends Annotation, M extends Annotation>
			(A childAnnotation, M metaAnnotation) implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			// 排除默认方法和Object方法
			if (method.getDeclaringClass() == Object.class) {
				return method.invoke(this, args);
			}

			try {
				// 1. 优先获取子注解的属性值
				Method childMethod = childAnnotation.annotationType()
						.getMethod(method.getName());
				Object value = childMethod.invoke(childAnnotation);
				if (value != null) {
					return value;
				}
			} catch (Exception ignored) {
				// 若子注解无该属性则忽略
			}

			// 2. 尝试获取元注解的默认值
			try {
				Method metaMethod = metaAnnotation.annotationType()
						.getMethod(method.getName());
				return metaMethod.invoke(metaAnnotation);
			} catch (Exception e) {
				throw new RuntimeException("元注解属性获取失败", e);
			}
		}
	}

}

