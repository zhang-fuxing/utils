package com.zhangfuxing.tools.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.*;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/4/17
 * @email zhangfuxing1010@163.com
 */
public class JsonObj extends JsonBase {
	private final Map<String, Object> object;


	private static class JsonObjSerializer extends JsonSerializer<JsonObj> {
		@Override
		public void serialize(JsonObj value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeObject(value.object);
		}
	}

	private static class JsonObjDeserializer extends JsonDeserializer<JsonObj> {
		@Override
		@SuppressWarnings("unchecked")
		public JsonObj deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			JsonObj jsonObj = new JsonObj();
			jsonObj.object.putAll(p.readValueAs(Map.class));
			return jsonObj;
		}
	}

	private JsonObj() {
		object = new LinkedHashMap<>();
		SimpleModule module = new SimpleModule();
		module.addSerializer(JsonObj.class, new JsonObjSerializer());
		module.addDeserializer(JsonObj.class, new JsonObjDeserializer());
		objectMapper.registerModule(module);
	}

	public static JsonObj create() {
		return new JsonObj();
	}

	public static JsonObj parse(Object obj) {
		try {
			var objects = create();
			if (obj == null) {
				return objects;
			} else if (obj instanceof String str) {
				return objects.objectMapper.readValue(str, JsonObj.class);
			} else if (obj instanceof File f) {
				return objects.objectMapper.readValue(f, JsonObj.class);
			} else if (obj instanceof URL url) {
				return objects.objectMapper.readValue(url, JsonObj.class);
			} else if (obj instanceof JsonParser jsonParser) {
				return objects.objectMapper.readValue(jsonParser, JsonObj.class);
			} else if (obj instanceof byte[] bytes) {
				return objects.objectMapper.readValue(bytes, JsonObj.class);
			} else if (obj instanceof InputStream inputStream) {
				return objects.objectMapper.readValue(inputStream, JsonObj.class);
			} else if (obj instanceof Reader reader) {
				return objects.objectMapper.readValue(reader, JsonObj.class);
			} else {
				String s = objects.objectMapper.writeValueAsString(obj);
				return objects.objectMapper.readValue(s, JsonObj.class);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonObj valueOf(Object obj) {
		return parse(obj);
	}

	public JsonObj config(Consumer<ObjectMapper> consumer) {
		consumer.accept(objectMapper);
		return this;
	}

	public JsonObj set(String key, Object value) {
		this.object.put(key, value);
		return this;
	}

	@Override
	public String toString() {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
