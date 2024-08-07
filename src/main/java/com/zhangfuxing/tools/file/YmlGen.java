package com.zhangfuxing.tools.file;

import com.zhangfuxing.tools.util.RefUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;

/**
 * yml文件生成器
 *
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/7
 * @email zhangfuxing1010@163.com
 */
public class YmlGen {
    private String retraction = " ".repeat(2);
    private String content = "";

    public YmlGen create(Iterable<?> writes) {
        for (Object obj : writes) {
            if (obj instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    write(entry.getKey().toString(), entry.getValue(), "");
                }
            } else if (obj instanceof Map.Entry<?,?> entry) {
                write(entry.getKey().toString(), entry.getValue(), "");
            }else {
                Field[] fields = RefUtil.getFields(obj);
                for (Field field : fields) {
                    String key = field.getName();
                    Object value = RefUtil.get(field, obj);
                    write(key, value, "");
                }
            }
        }
        return this;
    }

    public YmlGen create(Object obj) {
        List<Object> list = new ArrayList<>();
        list.add(obj);
        return create(list);
    }

    private void write(String key, Object value, String retraction) {
        if (value == null) addLine(buildLine(retraction, key, ""));
        else if (value instanceof String out) addLine(buildLine(retraction, key, "'%s'".formatted(out)));
        else if (value instanceof Number out) addLine(buildLine(retraction, key, out));
        else if (value instanceof Boolean out) addLine(buildLine(retraction, key, out));
        else if (value instanceof Map<?, ?> out) {
            addLine(buildLine(retraction, key, ""));
            for (Map.Entry<?, ?> entry : out.entrySet()) {
                String mapKey = entry.getKey().toString();
                Object mapValue = entry.getValue();
                write(mapKey, mapValue, retraction + this.retraction);
            }
        } else if (value instanceof List<?> out) {
            addLine(buildLine(retraction, key, ""));
            for (Object o : out) {
                write("- ", o, retraction + this.retraction);
            }
        } else {
            addLine(buildLine(retraction, key, ""));
            Field[] fields = RefUtil.getFields(value);
            for (Field field : fields) {
                Object next = RefUtil.get(field, value);
                write(field.getName(), next, retraction + this.retraction);
            }
        }
    }

    private void addLine(String line) {
        content = content + line + "\n";
    }

    private String buildLine(String retraction, String key, Object value) {
        if (key.trim().equals("-")) return retraction + key + value;
        return retraction + key + ": " + value;
    }

    public YmlGen setRetraction(int retraction) {
        this.retraction = " ".repeat(retraction);
        return this;
    }

    public YmlGen print() {
        System.out.println(content);
        return this;
    }

    public void toFile(File file) throws IOException {
        Objects.requireNonNull(file);
        Files.createDirectories(file.getParentFile().toPath());
        Files.writeString(file.toPath(), content);
    }

    public String getContent() {
        return content;
    }

}
