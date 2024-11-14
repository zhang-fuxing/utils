package com.zhangfuxing.tools.rpc;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhangfuxing.tools.excep.RemoteServiceCallException;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/11/13
 * @email zhangfuxing1010@163.com
 */
public class RpcMessageConvert {

    private static final String DATA = "data";
    private static final String CODE = "code";

    public static <T> T getObject(HttpResponse<String> response, Class<T> clazz) {
        responseHandler(response);
        JSONObject entries = JSONUtil.parseObj(response.body());
        if (!entries.containsKey(DATA)) {
            return null;
        }
        return JSONUtil.toBean(entries.getJSONObject(DATA), clazz);
    }

    public static <T> List<T> getList(HttpResponse<String> response, Class<T> clazz) {
        responseHandler(response);
        JSONObject entries = JSONUtil.parseObj(response.body());
        if (!entries.containsKey(DATA)) {
            return null;
        }
        return entries.getBeanList(DATA, clazz);
    }

    private static void  responseHandler(HttpResponse<?> response) {
       if (response == null || response.statusCode() != 200) {
           throw new RemoteServiceCallException();
       }
        Object body = response.body();
       if (body instanceof String stringBody) {
           JSONObject entries = JSONUtil.parseObj(stringBody);
           Integer code = entries.getInt(CODE);
           if (!Objects.equals(code, 200)) {
                throw new RemoteServiceCallException("远程调用错误：" + entries.getStr("msg"));
           }
       }

    }
}
