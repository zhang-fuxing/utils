package com.zhangfuxing.tools.http;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/15
 * @email zhangfuxing1010@163.com
 */
public class HttpBuilder {
    HttpClient.Builder httpClientBuilder;

    public static HttpBuilder client() {
        HttpBuilder httpBuilder = new HttpBuilder();
        httpBuilder.httpClientBuilder = HttpClient.newBuilder();
        return httpBuilder;
    }

    public HttpBuilder sslContext(SSLContext sslContext) {
        httpClientBuilder.sslContext(sslContext);
        return this;
    }

    public HttpBuilder version(HttpClient.Version version) {
        httpClientBuilder.version(version);
        return this;
    }

    public HttpBuilder cookieHandler(CookieHandler cookieHandler) {
        httpClientBuilder.cookieHandler(cookieHandler);
        return this;
    }

    public HttpBuilder connectTimeout(Duration duration) {
        httpClientBuilder.connectTimeout(duration);
        return this;
    }

    public HttpBuilder sslParameters(SSLParameters sslParameters) {
        httpClientBuilder.sslParameters(sslParameters);
        return this;
    }

    public HttpBuilder executor(Executor executor) {
        httpClientBuilder.executor(executor);
        return this;
    }

    public HttpBuilder followRedirects(HttpClient.Redirect followRedirects) {
        httpClientBuilder.followRedirects(followRedirects);
        return this;
    }

    public HttpBuilder priority(int priority) {
        httpClientBuilder.priority(priority);
        return this;
    }

    public HttpBuilder proxy(ProxySelector proxySelector) {
        httpClientBuilder.proxy(proxySelector);
        return this;
    }

    public HttpBuilder authenticator(Authenticator authenticator) {
        httpClientBuilder.authenticator(authenticator);
        return this;
    }

    public HttpRequestBuilder request() {
        return HttpRequestBuilder.create(this);
    }


}
