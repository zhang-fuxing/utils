package com.zhangfuxing.tools.db.handler;

import java.lang.reflect.Field;

/**
 * 字段值处理器接口
 */
public interface FieldValueHandler {
    /**
     * 处理字段值
     */
    Object handleValue(Object value, Field field);
    
    /**
     * 是否可以处理该字段
     */
    boolean canHandle(Field field);
} 