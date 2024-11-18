package com.zhangfuxing.tools.http;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/14
 * @email zhangfuxing1010@163.com
 */
public class HttpClientUtil {

    public static HttpClient.Builder createBuilder(boolean skipSSL) {
        HttpClient.Builder builder = HttpClient.newBuilder();
        if (skipSSL) {
            builder.sslContext(getUnsafeSSLContext());
        }
        return builder;
    }

    public static HttpClient.Builder createBuilder() {
        return createBuilder(false);
    }

    public static HttpClient createClient(boolean skipSSL) {
        return createBuilder(skipSSL).build();
    }

    public static HttpClient createClient() {
        return createClient(false);
    }

    public static SSLContext getUnsafeSSLContext() {
        SSLContext ssl;
        try {
            ssl = SSLContext.getInstance("SSL");
            ssl.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
        return ssl;
    }

    public static HttpBuilder client() {
        return HttpBuilder.client();
    }

    public static HttpRequestBuilder request() {
        return client().request();
    }

    public static String urlencoded(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null) {
                value = "";
            }
            sj.add(key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
        }

        return sj.toString();
    }
}
