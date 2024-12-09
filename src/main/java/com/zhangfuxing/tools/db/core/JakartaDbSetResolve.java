package com.zhangfuxing.tools.db.core;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/12/6
 * @email zhangfuxing1010@163.com
 */
public class JakartaDbSetResolve extends DbSetResolve {

    @Override
    public String getColumnName(Field field) {
        String result;
        String fieldName = field.getName();
        Column column = field.getAnnotation(Column.class);
        if (column != null) {
            String name = column.name();
            result = name.isBlank() ? fieldName : name;
        } else {
            result = fieldName;
        }
        return result;
    }

    @Override
    protected List<Field> getEntityColumnField(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class) ||
                                 field.isAnnotationPresent(Id.class))
                .collect(Collectors.toList());
    }
    
}
