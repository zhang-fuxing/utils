package com.zhangfuxing.tools.feignutil;

import com.zhangfuxing.tools.http.HttpClientUtil;
import feign.*;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.EncodeException;
import feign.form.FormEncoder;
import feign.form.spring.SpringFormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import static feign.Util.checkState;
import static feign.Util.emptyToNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/4/24
 * @email zhangfuxing1010@163.com
 */
public class FeignUtil {
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(FeignUtil.class);

	public static <T> T createClient(Class<T> interfaceClass, String targetURL, feign.Logger.Level level, boolean enableLog, Retryer retryer) {
		if (retryer == null) {
			retryer = new Retryer.Default();
		}

		Feign.Builder builder = Feign.builder()
				.client(new JdkHttpClient(enableLog))
				.encoder(new JacksonEncoder())
				.encoder(new FormEncoder(new SpringFormEncoder()))
				.decoder(new MessageDecoder(new JacksonDecoder()))
				.contract(new ISpringMvcContract())
				.retryer(retryer)
				.logger(new Slf4jLogger(interfaceClass))
				.logLevel(level);
		return builder.target(interfaceClass, targetURL);
	}

	public static <T> T createClient(Class<T> interfaceClass, String targetURL) {
		feign.Logger.Level level = Optional.ofNullable(interfaceClass.getAnnotation(FeignLog.class))
				.map(FeignLog::value)
				.orElse(feign.Logger.Level.NONE);
		Retryer.Default retryer = Optional.ofNullable(interfaceClass.getAnnotation(FeignRetryer.class))
				.map(f -> new Retryer.Default(f.period(), f.maxPeriod(), f.maxAttempts()))
				.orElse(null);
		return createClient(interfaceClass, targetURL, level, false, retryer);
	}

	public static <T> T createClient(Class<T> interfaceClass) {
		FeignTarget annotation = interfaceClass.getAnnotation(FeignTarget.class);
		String targetURL = Optional.ofNullable(annotation)
				.map(FeignTarget::value)
				.orElse(null);

		feign.Logger.Level level = Optional.ofNullable(interfaceClass.getAnnotation(FeignLog.class))
				.map(FeignLog::value)
				.orElse(feign.Logger.Level.NONE);

		Retryer.Default retryer = Optional.ofNullable(interfaceClass.getAnnotation(FeignRetryer.class))
				.map(f -> new Retryer.Default(f.period(), f.maxPeriod(), f.maxAttempts()))
				.orElse(null);

		return createClient(interfaceClass, targetURL, level, false, retryer);
	}

	private static class JdkHttpClient implements Client {
		private final HttpClient httpClient;
		private boolean enableLog;

		public JdkHttpClient() {
			this.httpClient = HttpClientUtil.createBuilder(true)
					.version(HttpClient.Version.HTTP_1_1)
					.build();
		}

		public JdkHttpClient(HttpClient httpClient) {
			this.httpClient = httpClient;
		}

		public JdkHttpClient(HttpClient httpClient, boolean enableLog) {
			this.httpClient = httpClient;
			this.enableLog = enableLog;
		}

		public JdkHttpClient(boolean enableLog) {
			this();
			this.enableLog = enableLog;
		}

		@Override
		public Response execute(Request request, Request.Options options) throws IOException {
			if (enableLog) {
				logger.warn("HttpClient Request: {} {}", request.httpMethod().name(), request.url());
			}
			HttpRequest httpRequest = buildHttpRequest(request);
			var httpResponse = sendRequest(httpRequest, options);
			return convertResponse(httpResponse, request);
		}

		private HttpRequest buildHttpRequest(Request request) {
			HttpRequest.Builder builder = HttpRequest.newBuilder()
					.uri(URI.create(request.url()))
					.method(request.httpMethod().name(), HttpRequest.BodyPublishers.noBody());

			// 设置请求头
			for (Map.Entry<String, Collection<String>> entry : request.headers().entrySet()) {
				String key = entry.getKey();
				Collection<String> values = entry.getValue();
				for (String value : values) {
					builder.header(key, value);
				}
			}

			// 设置请求体
			if (request.body() != null) {
				builder = builder.method(request.httpMethod().name(),
						HttpRequest.BodyPublishers.ofByteArray(request.body()));
			}

			return builder.build();
		}

		private HttpResponse<InputStream> sendRequest(HttpRequest request, Request.Options options) throws IOException {
			try {
				return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IOException("Interrupted while sending HTTP request", e);
			}
		}

		private Response convertResponse(HttpResponse<InputStream> httpResponse, Request request) throws IOException {
			int statusCode = httpResponse.statusCode();
			String reason = httpResponse.uri().toString();
			Map<String, Collection<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			HttpHeaders httpHeaders = httpResponse.headers();
			headers.putAll(httpHeaders.map());
			Response.Builder builder = Response.builder()
					.status(statusCode)
					.reason(reason)
					.headers(headers)
					.request(request);
			// 如果是stream响应头，返回原始InputStream
			if (isStreamContentType(headers.get("Content-Type"))) {
				InputStream body = httpResponse.body();
				String lenStr = httpHeaders.firstValue("Content-Length").orElse(null);
				Integer length = Optional.ofNullable(lenStr)
						.filter(s -> s.matches("\\d+"))
						.map(Integer::valueOf)
						.orElse(null);
				builder.body(body, length);
			}

			// 如果不是stream响应头，返回解码后的InputStream
			else {
				// 处理编码（GZIP/DEFLATE）
				try (InputStream inputStream = httpResponse.body()) {
					byte[] body = readAllBytesCompat(inputStream);
					List<String> encodings = httpHeaders.allValues("Content-Encoding");
					Collections.reverse(encodings);
					for (String encoding : encodings) {
						if ("gzip".equalsIgnoreCase(encoding)) {
							body = unGzip(body);
						} else if ("deflate".equalsIgnoreCase(encoding)) {
							body = unDeflate(body);
						}
					}
					builder.body(body);
				}
			}

			return builder.build();
		}

		// 新增判断方法：
		private boolean isStreamContentType(Collection<String> contentTypes) {
			return contentTypes != null && contentTypes.stream()
					.anyMatch(ct -> ct.startsWith("application/octet-stream")
									|| ct.contains("stream"));
		}

		private byte[] readAllBytesCompat(InputStream is) throws IOException {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] data = new byte[4096];
			int nRead;
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			return buffer.toByteArray();
		}

		private byte[] unGzip(byte[] data) {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				 GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(data))) {
				byte[] buffer = new byte[1024];
				int len;
				while ((len = gzis.read(buffer)) != -1) {
					baos.write(buffer, 0, len);
				}
				return baos.toByteArray();
			} catch (IOException e) {
				throw new EncodeException("Failed to un-gzip response body", e);
			}
		}


		private byte[] unDeflate(byte[] data) {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				 InflaterInputStream inf = new InflaterInputStream(new ByteArrayInputStream(data))) {
				byte[] buffer = new byte[1024];
				int len;
				while ((len = inf.read(buffer)) != -1) {
					baos.write(buffer, 0, len);
				}
				return baos.toByteArray();
			} catch (IOException e) {
				throw new EncodeException("Failed to un-deflate response body", e);
			}
		}
	}

	private static class ISpringMvcContract extends Contract.BaseContract {

		private boolean decodeSlash;

		@Override
		protected void processAnnotationOnClass(MethodMetadata data, Class<?> clz) {
			RequestMapping classAnnotation = findMergedAnnotation(clz, RequestMapping.class);
			if (classAnnotation != null) {
				logger.error("Cannot process class: {}. @RequestMapping annotation is not allowed on @FeignClient interfaces.", clz.getName());
				throw new IllegalArgumentException("@RequestMapping annotation not allowed on @FeignClient interfaces");
			}
		}

		@Override
		protected void processAnnotationOnMethod(MethodMetadata data, Annotation methodAnnotation, Method method) {
			if (!(methodAnnotation instanceof RequestMapping)
				&& !methodAnnotation.annotationType().isAnnotationPresent(RequestMapping.class)) {
				return;
			}
			RequestMapping methodMapping = findMergedAnnotation(method, RequestMapping.class);
			if (methodMapping == null) {
				return;
			}
			// HTTP Method
			RequestMethod[] methods = methodMapping.method();
			if (methods.length == 0) {
				methods = new RequestMethod[]{RequestMethod.GET};
			}
			checkOne(method, methods, "method");
			data.template().method(Request.HttpMethod.valueOf(methods[0].name()));
			checkAtMostOne(method, methodMapping.value(), "value");

			if (methodMapping.value().length > 0) {
				String pathValue = emptyToNull(methodMapping.value()[0]);
				if (pathValue != null) {
					pathValue = resolve(pathValue);
					// Append path from @RequestMapping if value is present on method
					if (!pathValue.startsWith("/") && !data.template().path().endsWith("/")) {
						pathValue = "/" + pathValue;
					}
					data.template().uri(pathValue, true);
					if (data.template().decodeSlash() != decodeSlash) {
						data.template().decodeSlash(decodeSlash);
					}
				}
			}

			// produces
			parseProduces(data, method, methodMapping);

			// consumes
			parseConsumes(data, method, methodMapping);

			// headers
			parseHeaders(data, method, methodMapping);

			data.indexToExpander(new LinkedHashMap<>());
			data.template().method(Request.HttpMethod.GET);
		}

		@Override
		protected boolean processAnnotationsOnParameter(MethodMetadata data, Annotation[] annotations, int paramIndex) {
			boolean result = false;
			for (Annotation parameterAnnotation : annotations) {
				result |= isSupportAnnotation(parameterAnnotation);
			}
			return result;
		}

		private boolean isSupportAnnotation(Annotation annotation) {
			return annotation instanceof RequestParam ||
				   annotation instanceof RequestHeader ||
				   annotation instanceof RequestBody ||
				   annotation instanceof PathVariable ||
				   annotation instanceof RequestPart;
		}


		private void checkOne(Method method, Object[] values, String fieldName) {
			checkState(values != null && values.length == 1, "Method %s can only contain 1 %s field. Found: %s",
					method.getName(), fieldName, values == null ? null : Arrays.asList(values));
		}

		private void checkAtMostOne(Method method, Object[] values, String fieldName) {
			checkState(values != null && (values.length == 0 || values.length == 1),
					"Method %s can only contain at most 1 %s field. Found: %s", method.getName(), fieldName,
					values == null ? null : Arrays.asList(values));
		}

		private String resolve(String value) {
			return value;
		}

		private void parseProduces(MethodMetadata md, Method method, RequestMapping annotation) {
			String[] serverProduces = annotation.produces();
			String clientAccepts = serverProduces.length == 0 ? null : emptyToNull(serverProduces[0]);
			if (clientAccepts != null) {
				md.template().header(ACCEPT, clientAccepts);
			}
		}

		private void parseConsumes(MethodMetadata md, Method method, RequestMapping annotation) {
			String[] serverConsumes = annotation.consumes();
			String clientProduces = serverConsumes.length == 0 ? null : emptyToNull(serverConsumes[0]);
			if (clientProduces != null) {
				md.template().header(CONTENT_TYPE, clientProduces);
			}
		}

		private void parseHeaders(MethodMetadata md, Method method, RequestMapping annotation) {
			for (String header : annotation.headers()) {
				if (header != null && (header.contains(":") || header.contains("="))) {
					String[] split = header.split("[:=]");
					md.template().header(split[0].trim(), split[1].trim());
				}
			}
		}
	}

	private record MessageDecoder(Decoder decoder) implements Decoder {

		@Override
		public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
			if (type.equals(Response.class) || type.equals(ResponseEntity.class)) {
				return response;
			}

			MediaType contentType = getContentType(response);
			if (type instanceof Class || type instanceof ParameterizedType || type instanceof WildcardType) {
				try (InputStream bodyStream = response.body().asInputStream()) {
					byte[] bytes = bodyStream.readAllBytes();
					if (streamMediaType(contentType)) {
						return new ByteArrayInputStream(bytes);
					} else if (jsonMediaType(contentType)) {
						return decoder.decode(response, type);
					} else if (isTextMediaType(contentType)) {
						return new String(bytes, StandardCharsets.UTF_8);
					} else {
						return bytes;
					}
				}

			}
			throw new DecodeException(response.status(), "type is not an instance of Class or ParameterizedType: " + type,
					response.request());
		}

		private boolean streamMediaType(MediaType mediaType) {
			return mediaType != null && mediaType.isCompatibleWith(MediaType.APPLICATION_OCTET_STREAM);
		}

		private boolean jsonMediaType(MediaType mediaType) {
			return mediaType != null && mediaType.isCompatibleWith(MediaType.APPLICATION_JSON);
		}

		private boolean isTextMediaType(MediaType mediaType) {
			return mediaType != null &&
				   (mediaType.isCompatibleWith(MediaType.TEXT_PLAIN) ||
					mediaType.isCompatibleWith(MediaType.TEXT_HTML) ||
					mediaType.isCompatibleWith(MediaType.TEXT_XML));
		}

		private MediaType getContentType(Response response) {
			Map<String, Collection<String>> headers = response.headers();
			Map<String, List<String>> map = new LinkedHashMap<>();
			for (Map.Entry<String, Collection<String>> entry : headers.entrySet()) {
				map.put(entry.getKey(), new ArrayList<>(entry.getValue()));
			}
			HttpHeaders httpHeaders = HttpHeaders.of(map, (name, values) -> true);
			String contentType = httpHeaders.firstValue("Content-Type").orElse(null);
			return contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.valueOf(contentType);
		}
	}
}
