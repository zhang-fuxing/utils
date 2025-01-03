package com.zhangfuxing.tools.db.core;

import javax.persistence.Column;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/12/6
 * @email zhangfuxing1010@163.com
 */
public class JavaxDbSetResolve extends DbSetResolve {

    private static final Logger log = LoggerFactory.getLogger(JavaxDbSetResolve.class);

    @Override
    public String getColumnName(Field field) {
        String result;
        String fieldName = field.getName();
        Column column = field.getAnnotation(Column.class);
        log.debug("Getting column name for field {}, has Column annotation: {}", 
            fieldName, column != null);
        
        if (column != null) {
            String name = column.name();
            log.debug("Column annotation name for field {}: '{}'", fieldName, name);
            result = name.isEmpty() ? fieldName : name;
        } else {
            result = fieldName;
        }
        log.debug("Final column name for field {}: {}", fieldName, result);
        return result;
    }

    @Override
    protected List<Field> getEntityColumnField(Class<?> entityClass) {
        log.debug("Scanning fields for entity class: {}", entityClass.getName());
        List<Field> fields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> {
                    boolean hasAnnotation = field.isAnnotationPresent(Column.class) ||
                                          field.isAnnotationPresent(Id.class);
                    log.debug("Field {} has annotation: {}", field.getName(), hasAnnotation);
                    return hasAnnotation;
                })
                .collect(Collectors.toList());
        
        log.debug("Found {} fields in entity {}: {}", fields.size(),
            entityClass.getSimpleName(),
            fields.stream().map(Field::getName).collect(Collectors.joining(", ")));
        return fields;
    }

}
