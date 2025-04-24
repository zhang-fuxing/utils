package com.zhangfuxing.tools.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/4/17
 * @email zhangfuxing1010@163.com
 */
public class JsonArr extends JsonBase implements Iterable<Object> {
	private final List<Object> list;

	private static class JsonArrSerializer extends JsonSerializer<JsonArr> {
		@Override
		public void serialize(JsonArr value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeObject(value.list);
		}
	}

	private static class JsonArrDeserializer extends JsonDeserializer<JsonArr> {
		@Override
		@SuppressWarnings("unchecked")
		public JsonArr deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			return new JsonArr(p.readValueAs(List.class));
		}
	}

	public static JsonArr parse(Object obj) {
		try {
			JsonArr objects = create();

			if (obj == null) {
				return objects;
			} else if (obj instanceof String str) {
				return objects.objectMapper.readValue(str, JsonArr.class);
			} else if (obj instanceof File f) {
				return objects.objectMapper.readValue(f, JsonArr.class);
			} else if (obj instanceof URL url) {
				return objects.objectMapper.readValue(url, JsonArr.class);
			} else if (obj instanceof JsonParser jsonParser) {
				return objects.objectMapper.readValue(jsonParser, JsonArr.class);
			} else if (obj instanceof byte[] bytes) {
				return objects.objectMapper.readValue(bytes, JsonArr.class);
			} else if (obj instanceof InputStream inputStream) {
				return objects.objectMapper.readValue(inputStream, JsonArr.class);
			} else if (obj instanceof Reader reader) {
				return objects.objectMapper.readValue(reader, JsonArr.class);
			} else {
				String s = objects.objectMapper.writeValueAsString(obj);
				return objects.objectMapper.readValue(s, JsonArr.class);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("JsonArr解析失败",e);
		}
	}

	public static JsonArr valueOf(Object obj) {
		return parse(obj);
	}

	private JsonArr() {
		this(new ArrayList<>());
	}

	private JsonArr(List<Object> list) {
		this.list = list;

		SimpleModule module = new SimpleModule();
		module.addSerializer(JsonArr.class, new JsonArrSerializer());
		module.addDeserializer(JsonArr.class, new JsonArrDeserializer());
		objectMapper.registerModule(module);
	}

	public static JsonArr create() {
		return new JsonArr();
	}

	public JsonArr config(Consumer<ObjectMapper> consumer) {
		consumer.accept(this.objectMapper);
		return this;
	}

	public JsonArr add(int index, Object value) {
		this.list.set(index, value);
		return this;
	}

	public JsonArr add(Object value) {
		this.list.add(value);
		return this;
	}

	@Override
	public Iterator<Object> iterator() {
		return this.list.iterator();
	}

	@Override
	public String toString() {
		try {
			return this.objectMapper.writeValueAsString(this.list);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
