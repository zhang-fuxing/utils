package com.zhangfuxing.tools.plus;

import java.util.*;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/27
 * @email zhangfuxing1010@163.com
 */
public class BiMap<K, F, V> {
    private final Map<K, Map<F, V>> value;
    private final transient List<BiMapItem<K, F, V>> items;
    private int type = 0;

    private BiMap(Map<K, Map<F, V>> value) {
        this.value = value;
        items = new ArrayList<>();
    }

    public static <K, F, V> BiMap<K, F, V> createLinkedBiMap() {
        BiMap<K, F, V> res = new BiMap<>(new LinkedHashMap<>());
        res.type = 1;
        return res;
    }

    public static <K, F, V> BiMap<K, F, V> createHashBiMap() {
        BiMap<K, F, V> res = new BiMap<>(new HashMap<>());
        res.type = 0;
        return res;
    }

    public V get(K key, F field) {
        return Optional.ofNullable(value.get(key)).map(i -> i.get(field)).orElse(null);
    }

    public Map<F, V> get(K key) {
        return value.get(key);
    }

    public void put(K key, F field, V value) {
        Map<F, V> fvMap = this.value.computeIfAbsent(key, k -> type == 0 ? new HashMap<>() : new LinkedHashMap<>());
        fvMap.put(field, value);
        BiMapItem<K, F, V> biEl = new BiMapItem<>(key, field, value);
        if (!items.contains(biEl)) {
            items.add(biEl);
        } else {
            items.remove(biEl);
            items.add(biEl);
        }
    }

    public void put(K key, Map<F, V> fvMap) {
        value.put(key, fvMap);
        if (fvMap != null) {
            fvMap.forEach((field,value_) -> {
                BiMapItem<K, F, V> biEl = new BiMapItem<>(key, field, value_);
                if (!items.contains(biEl)) {
                    items.add(biEl);
                } else {
                    items.remove(biEl);
                    items.add(biEl);
                }
            });
        }
    }

    public Map<F, V> remove(K key) {
        Map<F, V> remove = value.remove(key);
        if (remove != null) {
            remove.forEach((field,value_) -> {
                BiMapItem<K, F, V> biEl = new BiMapItem<>(key, field, value_);
                items.remove(biEl);
            });
        }
        return remove;
    }

    public V remove(K key, F field) {
        V temp = Optional.ofNullable(value.get(key)).map(i -> i.get(field)).orElse(null);
        var item = new BiMapItem<>(key, field, temp);
        items.remove(item);
        return temp;
    }

    public void clear() {
        value.clear();
        items.clear();
    }

    public void forEach(ThreeConsumer<? super K, ? super F, ? super V> action) {
        value.forEach((key, innerValue) -> innerValue.forEach((field, fieldValue) -> action.accept(key, field, fieldValue)));
    }

    public Collection<V> values(K key) {
        return Optional.ofNullable(value.get(key))
                .map(Map::values)
                .orElseGet(Collections::emptyList);
    }

    public Set<F> fieldSet(K key) {
        return Optional.ofNullable(value.get(key)).map(Map::keySet).orElse(null);
    }

    public Set<K> keySet() {
        return value.keySet();
    }

    public Map<K, Map<F, V>> getValue() {
        return value;
    }

    public int size() {
        return value.size();
    }

    public int size(K key) {
        return Optional.ofNullable(value.get(key)).map(Map::size).orElse(0);
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public boolean isEmpty(K key) {
        return Optional.ofNullable(value.get(key)).map(Map::isEmpty).orElse(true);
    }

    public boolean containsKey(K key) {
        return value.containsKey(key);
    }

    public boolean containsKey(K key, F field) {
        return Optional.ofNullable(value.get(key)).map(m -> m.containsKey(field)).orElse(false);
    }

    public boolean containsValue(K key, V value) {
        return Optional.ofNullable(this.value.get(key)).map(m -> m.containsValue(value)).orElse(false);
    }

    public List<BiMapItem<K, F, V>> items() {
        return items;
    }
}
