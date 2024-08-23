package com.zhangfuxing.tools.tree;

import java.io.Serializable;
import java.util.*;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/7/25
 * @email zhangfuxing1010@163.com
 */
public interface TreeSupport<T extends TreeSupport<T>> {
    void setId(Serializable id);

    Serializable getId();

    void setParentId(Serializable parentId);

    Serializable getParentId();

    List<T> getChild();

    void addChild(T item);

    Serializable getRootParentId();

   static <T extends TreeSupport<T>> List<T> getTree(List<T> data) {
        if (data == null || data.isEmpty()) return new ArrayList<>(0);
        Map<Serializable, T> map = new HashMap<>();
        for (T datum : data) {
            Serializable id = datum.getId();
            map.put(id, datum);
        }
        List<T> roots = new ArrayList<>();
        for (T datum : data) {
            Serializable parentId = datum.getParentId();
            Serializable rootParentId = datum.getRootParentId();
            if (Objects.equals(parentId, rootParentId)) {
                roots.add(datum);
                continue;
            }
            T childParent = map.get(parentId);
            if (childParent == null) {
                if (datum.noneParentAddToRoot()) {
                    roots.add(datum);
                    datum.setParentId(datum.getRootParentId());
                }
                continue;
            }
            childParent.addChild(datum);
        }
        return roots;
    }

    default boolean noneParentAddToRoot() {
        return true;
    }
}
