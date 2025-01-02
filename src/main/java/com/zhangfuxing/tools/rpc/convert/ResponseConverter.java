package com.zhangfuxing.tools.rpc.convert;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhangfuxing.tools.excep.RemoteServiceCallException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

/**
 * 响应消息转换器
 */
public class ResponseConverter {
    private static final String DATA = "data";
    private static final String CODE = "code";
    private static final String MSG = "msg";

    /**
     * 转换响应结果
     *
     * @param response 响应对象
     * @param method 方法对象
     * @return 转换后的对象
     */
    public static Object convert(HttpResponse<?> response, java.lang.reflect.Method method) {
        if (response == null || response.statusCode() != 200) {
            throw new RemoteServiceCallException("HTTP请求失败，状态码：" + (response != null ? response.statusCode() : "null"));
        }

        Class<?> returnType = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();

        // 处理void返回类型
        if (returnType == Void.TYPE) {
            return null;
        }

        // 处理原始响应类型
        if (HttpResponse.class.isAssignableFrom(returnType)) {
            return response;
        }

        Object body = response.body();
        if (body == null) {
            return null;
        }

        // 1. 处理直接返回类型
        if (returnType.isInstance(body)) {
            return body;
        }

        // 2. 处理基本类型
        if (isPrimitiveOrWrapper(returnType)) {
            return convertToPrimitive(body, returnType);
        }

        // 3. 处理String类型
        if (String.class.equals(returnType)) {
            return body instanceof String ? body : String.valueOf(body);
        }

        // 4. 处理List类型
        if (List.class.isAssignableFrom(returnType)) {
            if (genericReturnType instanceof ParameterizedType pt) {
                Type elementType = pt.getActualTypeArguments()[0];
                if (elementType instanceof Class<?>) {
                    return convertToList(body, (Class<?>) elementType);
                }
            }
            throw new RemoteServiceCallException("无法确定List的元素类型");
        }

        // 5. 处理JSON转换
        try {
            if (body instanceof String stringBody) {
                if (isJsonString(stringBody)) {
                    return convertFromJson(stringBody, returnType);
                } else {
                    // 如果不是JSON字符串，直接返回字符串
                    return stringBody;
                }
            }
        } catch (Exception e) {
            // JSON转换失败，返回原始内容
            return body;
        }

        return body;
    }

    private static boolean isJsonString(String str) {
        str = str.trim();
        return (str.startsWith("{") && str.endsWith("}"))
                || (str.startsWith("[") && str.endsWith("]"));
    }

    private static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() 
                || Number.class.isAssignableFrom(type)
                || Boolean.class.equals(type)
                || Character.class.equals(type);
    }

    private static Object convertToPrimitive(Object value, Class<?> targetType) {
        String strValue = String.valueOf(value);
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(strValue);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(strValue);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(strValue);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(strValue);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(strValue);
        } else if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(strValue);
        } else if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(strValue);
        } else if (targetType == char.class || targetType == Character.class) {
            return strValue.charAt(0);
        }
        throw new RemoteServiceCallException("不支持的基本类型转换：" + targetType);
    }

    private static <T> List<T> convertToList(Object body, Class<T> elementType) {
        if (body instanceof String stringBody) {
            if (!isJsonString(stringBody)) {
                throw new RemoteServiceCallException("响应体不是有效的JSON格式");
            }
            JSONObject jsonObject = JSONUtil.parseObj(stringBody);
            validateResponse(jsonObject);
            if (!jsonObject.containsKey(DATA)) {
                return List.of();
            }
            return jsonObject.getBeanList(DATA, elementType);
        }
        throw new RemoteServiceCallException("响应体不是String类型，无法转换为List");
    }

    private static <T> T convertFromJson(String jsonString, Class<T> targetType) {
        JSONObject jsonObject = JSONUtil.parseObj(jsonString);
        validateResponse(jsonObject);

        if (!jsonObject.containsKey(DATA)) {
            return null;
        }

        Object data = jsonObject.get(DATA);
        if (data == null) {
            return null;
        }

        // 如果目标类型就是JSONObject，直接返回
        if (JSONObject.class.isAssignableFrom(targetType)) {
            return targetType.cast(data instanceof JSONObject ? data : JSONUtil.parseObj(data));
        }

        // 转换为目标类型
        return JSONUtil.toBean(data instanceof JSONObject ? (JSONObject)data : JSONUtil.parseObj(data), targetType);
    }

    private static void validateResponse(JSONObject response) {
        Integer code = response.getInt(CODE);
        if (!Objects.equals(code, 200)) {
            String msg = response.getStr(MSG, "未知错误");
            throw new RemoteServiceCallException(String.format("远程调用错误 - 代码：%d，消息：%s", code, msg));
        }
    }
} 