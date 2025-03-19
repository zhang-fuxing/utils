package com.zhangfuxing.tools.rpc.convert;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhangfuxing.tools.excep.RemoteServiceCallException;
import com.zhangfuxing.tools.rpc.Ref;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 响应消息转换器
 */
public class ResponseConverter {
	private static final String DATA = "data";
	private static final String LIST = "list";
	private static final String RECORDS = "records";
	private static final String CODE = "code";
	private static final String MSG = "msg";

	/**
	 * 转换响应结果
	 *
	 * @param response 响应对象
	 * @param method   方法对象
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
					return convertFromJson(stringBody, method);
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
			if (jsonObject.containsKey(DATA))
				return jsonObject.getBeanList(DATA, elementType);
			else if (jsonObject.containsKey(LIST))
				return jsonObject.getBeanList(LIST, elementType);
			else if (jsonObject.containsKey(RECORDS))
				return jsonObject.getBeanList(RECORDS, elementType);
			else
				return new ArrayList<>(0);
		}
		throw new RemoteServiceCallException("响应体不是String类型，无法转换为List");
	}

	@SuppressWarnings("unchecked")
	private static <T> T convertFromJson(String jsonString, Method method) {
		JSONObject jsonObject = JSONUtil.parseObj(jsonString);
		validateResponse(jsonObject);

		if (!jsonObject.containsKey(DATA) &&
			!jsonObject.containsKey(LIST) &&
			!jsonObject.containsKey(RECORDS)) {
			return null;
		}

		Object data = jsonObject.containsKey(DATA) ? jsonObject.get(DATA) :
				jsonObject.containsKey(LIST) ? jsonObject.get(LIST) :
						jsonObject.get(RECORDS);
		if (data == null) {
			return null;
		}
		Class<T> targetType = (Class<T>) method.getReturnType();
		Type genericType = method.getGenericReturnType();
		if (Ref.class.equals(targetType)) {
			if (genericType instanceof ParameterizedType pt) {
				Type actualType = pt.getActualTypeArguments()[0];
				Object convertedData = Convert.convert(actualType, data);
				return (T) new Ref<>(convertedData);
			}
			throw new RemoteServiceCallException("Ref必须指定泛型类型");
		}

		// 如果目标类型就是JSONObject，直接返回
		if (JSONObject.class.isAssignableFrom(targetType)) {
			return targetType.cast(data instanceof JSONObject ? data : JSONUtil.parseObj(data));
		}
		if (JSONArray.class.isAssignableFrom(targetType)) {
			return targetType.cast(data instanceof JSONArray ? data : JSONUtil.parseArray(data));
		}

		// 转换为目标类型
		if (data instanceof JSONObject o) {
			return JSONUtil.toBean(o, targetType);
		} else if (data instanceof JSONArray a) {
			return (T) a.toList(targetType);
		} else {
			return JSONUtil.toBean(JSONUtil.parseObj(data), targetType);
		}
	}

	private static void validateResponse(JSONObject response) {
		Integer code = response.getInt(CODE);
		if (!Objects.equals(code, 200)) {
			String msg = response.getStr(MSG, "未知错误");
			throw new RemoteServiceCallException(String.format("远程调用错误 - 代码：%d，消息：%s", code, msg));
		}
	}
} 