package com.zhangfuxing.tools.group;

import com.zhangfuxing.tools.lambda.LambdaUtil;
import com.zhangfuxing.tools.lambda.SFun;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/7/29
 * @email zhangfuxing1010@163.com
 */
public class GroupBuilder<T> {

    private List<T> groupData;
    LinkedBlockingQueue<GroupKey<T, ?>> groupQueue = new LinkedBlockingQueue<>();

    public static <T> GroupBuilder<T> create(List<T> list) {
        GroupBuilder<T> builder = new GroupBuilder<>();
        builder.groupData = list;
        return builder;
    }

    public <K> GroupKey<T, K> groupBy(SFun<T, K> sFun) {
        GroupKey<T, K> tkGroupKey = new GroupKey<>(sFun);
        tkGroupKey.builder = this;
        return tkGroupKey;
    }

    public Collection<Map<String, Object>> build() {
        GroupKey<T, ?> poll = groupQueue.poll();
        if (poll == null) {
            throw new IllegalStateException("未指定分组数据");
        }
        return build(poll, groupData);
    }

    @SuppressWarnings("unchecked")
    public Collection<Map<String, Object>> build(GroupKey<T, ?> groupKey, List<T> data) {
        if (groupKey == null || data == null) {
            throw new IllegalArgumentException("GroupKey or data cannot be null");
        }
        Map<Object, Map<String, Object>> map = new LinkedHashMap<>();
        for (T datum : data) {
            Object mainKey = groupKey.keyExt.apply(datum);
            String mainName = LambdaUtil.getName(groupKey.keyExt);
            Map<String, Object> stringObjectMap = map.get(mainKey);
            if (stringObjectMap == null) {
                Map<String, Object> g = new LinkedHashMap<>();
                g.put(mainName, mainKey);
                for (SFun<T, ?> otherExt : groupKey.ext) {
                    Object value = otherExt.apply(datum);
                    String key = LambdaUtil.getName(otherExt);
                    g.put(key, value);
                }
                if (groupKey.child.isEmpty()) {
                    List<Object> cgList = new ArrayList<>();
                    cgList.add(datum);
                    g.put(groupKey.childName, cgList);
                } else {
                    Map<String, Object> cg = new LinkedHashMap<>();
                    for (SFun<T, ?> c : groupKey.child) {
                        Object cValue = c.apply(datum);
                        String cKey = LambdaUtil.getName(c);
                        cg.put(cKey, cValue);
                    }
                    List<Object> cgList = new ArrayList<>();
                    cgList.add(cg);
                    g.put(groupKey.childName, cgList);
                }
                map.put(mainKey, g);
            } else {
                if (groupKey.child.isEmpty()) {
                    List<Object> cgList = (List<Object>) stringObjectMap.get(groupKey.childName);
                    cgList.add(datum);
                    stringObjectMap.put(groupKey.childName, cgList);
                } else {
                    Map<String, Object> cg = new LinkedHashMap<>();
                    for (SFun<T, ?> c : groupKey.child) {
                        Object cValue = c.apply(datum);
                        String cKey = LambdaUtil.getName(c);
                        cg.put(cKey, cValue);
                    }
                    List<Object> cgList = (List<Object>) stringObjectMap.get(groupKey.childName);
                    cgList.add(cg);
                    stringObjectMap.put(groupKey.childName, cgList);
                }
            }
        }
        Collection<Map<String, Object>> values = map.values();
        if (!groupQueue.isEmpty()) {
            GroupKey<T, ?> next = groupQueue.poll();
            for (Map<String, Object> value : values) {
                Object o = value.get(groupKey.childName);
                if (o instanceof List<?>) {
                    Collection<Map<String, Object>> build;
                    try {
                        build = build(next, (List<T>) o);
                    } catch (Exception e) {
                        throw new RuntimeException("请不要在最后一个分组前添加子元素的字段");
                    }
                    value.put(groupKey.childName, build);
                } else {
                    throw new IllegalStateException("Expected a List but found: " + o.getClass());
                }
            }
        }
        return values;
    }
}
