package com.zhangfuxing.tools.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/1/4
 * @email zhangfuxing@kingshine.com.cn
 */
public class HttpConfig {
    /**
     * GET | POST
     */
    private String method;
    /**
     * 请求头
     */
    private Map<String, String> headers;
    /**
     * 响应数据处理器
     */
    private HttpResponse.BodyHandler<?> bodyHandler;
    /**
     * 请求体数据
     */
    private Object body;
    /**
     * 请求体数据类型
     */
    private Class<?> bodyType;

    /**
     * 请求体数据提供者
     */
    private HttpRequest.BodyPublisher bodyPublisher;
    /**
     * http版本
     */
    private HttpClient.Version version;

    /**
     * 自定义HttpClient
     */
    private HttpClient client;

    public HttpConfig() {
    }

    public HttpConfig(String method, Map<String, String> headers, HttpResponse.BodyHandler<?> bodyHandler, Object body, HttpRequest.BodyPublisher bodyPublisher, HttpClient.Version version, HttpClient client) {
        this.method = method;
        this.headers = headers;
        this.bodyHandler = bodyHandler;
        this.body = body;
        this.bodyPublisher = bodyPublisher;
        this.version = version;
        this.client = client;
    }

    public static HttpConfigBuilder newBuilder() {
        return new HttpConfigBuilder();
    }

    public HttpRequest.BodyPublisher getBodyPublisher() {
        HttpRequest.BodyPublisher result;
        if (this.bodyPublisher != null) {
            result = this.bodyPublisher;
        } else if (body == null) {
            result = HttpRequest.BodyPublishers.noBody();
        } else {
            HttpRequest.BodyPublisher publisher;
            if (bodyType == null) {
                publisher = HttpRequest.BodyPublishers.ofString(body.toString());
            } else if (bodyType == InputStream.class) {
                publisher = HttpRequest.BodyPublishers.ofInputStream(() -> ((InputStream) body));
            } else if (byte[].class == bodyType) {
                publisher = HttpRequest.BodyPublishers.ofByteArray((byte[]) body);
            } else if (File.class == bodyType) {
                try {
                    publisher = HttpRequest.BodyPublishers.ofFile((Path) body);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else if (String.class == bodyType) {
                publisher = HttpRequest.BodyPublishers.ofString(body.toString());
            } else {
                throw new RuntimeException("不支持的bodyType:" + bodyType);
            }
            result = publisher;
        }
        return result;
    }

    public HttpResponse.BodyHandler<?> getBodyHandler() {
        return this.bodyHandler;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setBodyHandler(HttpResponse.BodyHandler<?> bodyHandler) {
        this.bodyHandler = bodyHandler;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Class<?> getBodyType() {
        return bodyType;
    }

    public void setBodyType(Class<?> bodyType) {
        this.bodyType = bodyType;
    }

    public void setBodyPublisher(HttpRequest.BodyPublisher bodyPublisher) {
        this.bodyPublisher = bodyPublisher;
    }

    public HttpClient.Version getVersion() {
        return version;
    }

    public void setVersion(HttpClient.Version version) {
        this.version = version;
    }

    public HttpClient getClient() {
        return client;
    }

    public void setClient(HttpClient client) {
        this.client = client;
    }
}
