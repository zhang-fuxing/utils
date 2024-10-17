package com.zhangfuxing.tools.rpc;

import cn.hutool.json.JSONUtil;
import com.zhangfuxing.tools.http.CommonResponseHandler;
import com.zhangfuxing.tools.http.HttpClientUtil;
import com.zhangfuxing.tools.rpc.anno.RpcBody;
import com.zhangfuxing.tools.rpc.anno.RpcMapping;
import com.zhangfuxing.tools.rpc.anno.RpcParam;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/17
 * @email zhangfuxing1010@163.com
 */
public class RpcInvocationHandler implements InvocationHandler {
    private String basURL;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        String uri = Optional.ofNullable(declaringClass.getAnnotation(RpcMapping.class))
                .map(RpcMapping::value)
                .orElse("");
        RpcMapping rpcMapping = method.getAnnotation(RpcMapping.class);
        uri = uri + rpcMapping.value();
        RpcHeader rpcHeaders = null;
        StringJoiner urlParams = new StringJoiner("&");
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();
        Map<String, String> cookieHead = null;
        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = method.getParameters()[i];
            Object arg = args[i];
            RpcParam annotation = parameter.getAnnotation(RpcParam.class);
            if (annotation != null) {
                String paraName = annotation.value();
                if (arg == null && annotation.required()) {
                    throw new IllegalArgumentException("参数：%s 是必须的，如果不需要此参数，请设置 @RpcParam(required=false)");
                }
                urlParams.add(paraName + "=" + URLEncoder.encode(String.valueOf(arg), StandardCharsets.UTF_8));
            }

            RpcBody rpcBody = parameter.getAnnotation(RpcBody.class);
            if (rpcBody != null) {
                RpcBodyType value = rpcBody.value();
                switch (value) {
                    case JSON ->
                            bodyPublisher = HttpRequest.BodyPublishers.ofString(JSONUtil.toJsonStr(arg), Charset.forName(rpcBody.charset()));
                    case INPUT_STREAM -> bodyPublisher = HttpRequest.BodyPublishers.ofInputStream(() -> {
                        if (arg instanceof InputStream inputStream) {
                            return inputStream;
                        } else {
                            throw new IllegalArgumentException("请求体参数不是InputStream对象");
                        }
                    });
                    case TEXT -> {
                        Charset charset = Charset.forName(rpcBody.charset());
                        if (arg instanceof String str) {
                            bodyPublisher = HttpRequest.BodyPublishers.ofString(str, charset);
                        } else if (arg instanceof Map<?, ?> map) {
                            StringJoiner joiner = new StringJoiner("&");
                            for (Map.Entry<?, ?> entry : map.entrySet()) {
                                joiner.add(entry.getKey() + "=" + URLEncoder.encode(String.valueOf(entry.getValue()), charset));
                            }
                            bodyPublisher = HttpRequest.BodyPublishers.ofString(joiner.toString(), charset);
                        }
                    }
                    case NONE -> bodyPublisher = HttpRequest.BodyPublishers.noBody();
                }
            }
            if (arg instanceof RpcCookie cookies) {
                cookieHead = cookies.getCookies();
            }
            if (arg instanceof RpcHeader rpcHeader) {
                rpcHeaders = rpcHeader;
            }
        }
        if (!urlParams.toString().isBlank()) {
            uri = uri + "?" + urlParams;
        }
        if (method.getReturnType() == Void.TYPE) {
            return null;
        }
        var target = this.basURL + uri;
        var builder = HttpClientUtil.client()
                .request()
                .url(target)
                .method(rpcMapping.method().name(), bodyPublisher);
        for (String header : rpcMapping.headers()) {
            if (header != null && header.contains(":")) {
                String[] split = header.split(":");
                builder.header(split[0], split[1]);
            }
        }
        if (cookieHead != null) {
            StringJoiner joiner = new StringJoiner("; ");
            for (Map.Entry<String, String> entry : cookieHead.entrySet()) {
                joiner.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
            builder.header(RpcCookie.COOKIE, joiner.toString());
        }
        if (rpcHeaders != null) {
            rpcHeaders.getHeader().forEach(builder::header);
        }
        Class<?> returnType = rpcMapping.responseType();
        HttpResponse.BodyHandler<?> bodyHandler = CommonResponseHandler.of(returnType);
        return builder.response(bodyHandler);
    }

    public String getBasURL() {
        return basURL;
    }

    public void setBasURL(String basURL) {
        this.basURL = basURL;
    }
}
