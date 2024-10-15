package com.zhangfuxing.tools.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/15
 * @email zhangfuxing1010@163.com
 */
public class HttpRequestBuilder {
    private final HttpRequest.Builder builderRequest;
    HttpBuilder httpBuilder;

    private HttpRequestBuilder(HttpRequest.Builder builderRequest) {
        this.builderRequest = builderRequest;
    }

    static HttpRequestBuilder create(HttpBuilder httpBuilder) {
        HttpRequestBuilder builder = new HttpRequestBuilder(HttpRequest.newBuilder());
        builder.httpBuilder = httpBuilder;
        return builder;
    }

    public HttpRequestBuilder url(String url) {
        builderRequest.uri(URI.create(url));
        return this;
    }

    public HttpRequestBuilder expectContinue(boolean enable) {
        builderRequest.expectContinue(enable);
        return this;
    }

    public HttpRequestBuilder version(HttpClient.Version version) {
        builderRequest.version(version);
        return this;
    }


    public HttpRequestBuilder header(String name, String value) {
        builderRequest.header(name, value);
        return this;
    }

    public HttpRequestBuilder headers(String... headers) {
        builderRequest.headers(headers);
        return this;
    }

    public HttpRequestBuilder timeout(Duration duration) {
        builderRequest.timeout(duration);
        return this;
    }

    public HttpRequestBuilder setHeader(String name, String value) {
        builderRequest.header(name, value);
        return this;
    }

    public HttpRequestBuilder GET() {
        builderRequest.GET();
        return this;
    }

    public HttpRequestBuilder POST(HttpRequest.BodyPublisher bodyPublisher) {
        builderRequest.POST(bodyPublisher);
        return this;
    }

    public HttpRequestBuilder PUT(HttpRequest.BodyPublisher bodyPublisher) {
        builderRequest.PUT(bodyPublisher);
        return this;
    }

    public HttpRequestBuilder DELETE() {
        builderRequest.DELETE();
        return this;
    }

    public HttpRequestBuilder method(String method, HttpRequest.BodyPublisher bodyPublisher) {
        builderRequest.method(method, bodyPublisher);
        return this;
    }

    public HttpRequestBuilder copy() {
        builderRequest.copy();
        return this;
    }


    /**
     * 例：HttpResponse.BodyHandlers.ofString()
     *
     * @param bodyHandler 响应体处理器
     * @param <T>         响应类型
     * @return 响应
     * @throws IOException          IO异常
     * @throws InterruptedException 中断异常
     */
    public <T> HttpResponse<T> response(HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        HttpClient httpClient = this.httpBuilder.httpClientBuilder.build();
        HttpRequest request = this.builderRequest.build();
        return httpClient.send(request, bodyHandler);
    }

}
