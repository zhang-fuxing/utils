package com.zhangfuxing.tools.plus;

import java.util.Objects;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/28
 * @email zhangfuxing1010@163.com
 */
public class BiMapItem<K,F,V> {
    private K key;
    private F field;
    private V value;

    public BiMapItem() {
    }

    public BiMapItem(K key, F field, V value) {
        this.key = key;
        this.field = field;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public F getField() {
        return field;
    }

    public void setField(F field) {
        this.field = field;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BiMapItem<?, ?, ?> biMapItem)) return false;

        return Objects.equals(key, biMapItem.key) && Objects.equals(field, biMapItem.field);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(key);
        result = 31 * result + Objects.hashCode(field);
        return result;
    }

    @Override
    public String toString() {
        return "BiMapItem{" +
               "key=" + key +
               ", field=" + field +
               ", value=" + value +
               '}';
    }
}
