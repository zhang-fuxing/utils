package com.zhangfuxing.tools.http;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class HttpConfigBuilder {
    private String method;
    private Map<String, String> headers;
    private HttpResponse.BodyHandler<?> bodyHandler;
    private Object body;
    private HttpRequest.BodyPublisher bodyPublisher;
    private HttpClient.Version version = HttpClient.Version.HTTP_1_1;
    private HttpClient client;

    public HttpConfigBuilder setMethod(String method) {
        this.method = method;
        return this;
    }

    public HttpConfigBuilder setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpConfigBuilder setBodyHandler(HttpResponse.BodyHandler<?> bodyHandler) {
        this.bodyHandler = bodyHandler;
        return this;
    }

    public HttpConfigBuilder setBody(Object body) {
        this.body = body;
        return this;
    }

    public HttpConfigBuilder setBodyPublisher(HttpRequest.BodyPublisher bodyPublisher) {
        this.bodyPublisher = bodyPublisher;
        return this;
    }

    public HttpConfigBuilder setVersion(HttpClient.Version version) {
        this.version = version;
        return this;
    }

    public HttpConfigBuilder setClient(HttpClient client) {
        this.client = client;
        return this;
    }

    public HttpConfig build() {
        return new HttpConfig(method, headers, bodyHandler, body, bodyPublisher, version, client);
    }
}