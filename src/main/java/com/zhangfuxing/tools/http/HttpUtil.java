package com.zhangfuxing.tools.http;


import com.zhangfuxing.tools.util.ChainUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Optional;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/1/3
 * @email zhangfuxing@kingshine.com.cn
 */
@SuppressWarnings("unchecked")
public class HttpUtil {
    static HttpClient defClient = HttpClient.newBuilder().build();
    static HttpConfig defConf = HttpConfig.newBuilder()
            .setMethod("GET")
            .build();

    public static <T> HttpResponse<T> send(String url, HttpConfig config) throws IOException, InterruptedException {
        HttpRequest request = ChainUtil
                .consumerChain(HttpRequest.newBuilder())
                .then(builder -> builder.uri(URI.create(url)))
                .then(builder -> {
                    if ("GET".equalsIgnoreCase(config.getMethod())) {
                        builder.GET();
                    } else {
                        builder.POST(config.getBodyPublisher());
                    }
                })
                .then(builder -> Optional.ofNullable(config.getHeaders()).ifPresent(headers -> headers.forEach(builder::setHeader)))
                .then(builder -> builder.version(config.getVersion()))
                .map(HttpRequest.Builder::build)
                .get();
        HttpClient client = Objects.requireNonNullElse(config.getClient(), defClient);
        return client.send(request, (HttpResponse.BodyHandler<T>) config.getBodyHandler());
    }

    public static <T> HttpResponse<T> get(String url, HttpConfig config) throws IOException, InterruptedException {
        return send(url, config);
    }

    public static <T> HttpResponse<T> get(String url) throws IOException, InterruptedException {
        return send(url, defConf);
    }

    public static <T> HttpResponse<T> post(String url, HttpConfig config) throws IOException, InterruptedException {
        return send(url, config);
    }

    public static <T> HttpResponse<T> post(String url, Object body) throws IOException, InterruptedException {
        HttpConfig build = HttpConfig.newBuilder()
                .setBody(body)
                .build();
        return send(url,build);
    }
}
