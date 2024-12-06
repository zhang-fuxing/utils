package com.zhangfuxing.tools.group;

import com.zhangfuxing.tools.lambda.LambdaUtil;
import com.zhangfuxing.tools.util.RefUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/7/29
 * @email zhangfuxing1010@163.com
 */
public class GroupBuilder<T, R> {

    private List<T> groupData;
    LinkedBlockingQueue<GroupRules<T, ?>> groupQueue = new LinkedBlockingQueue<>();
    LinkedBlockingQueue<GroupBenRules<T, ?, ?, ?>> groupQueueBean = new LinkedBlockingQueue<>();
    private Class<R> rclass;

    /**
     * 创建一个普通的分组对象，分组的数据会被保持到 map 集合中
     *
     * @param list 待分组的数据列表
     * @param <T>  数据的类型
     * @param <R>  顶级分组返回类型
     * @return 分组构造对象
     */
    public static <T, R> GroupBuilder<T, R> create(List<T> list) {
        GroupBuilder<T, R> builder = new GroupBuilder<>();
        builder.groupData = list;
        return builder;
    }

    /**
     * 创建一个分组构造者实例对象，该对象可对一组数据进行分组并转换到对应的实体类
     *
     * @param list   待分组的数据列表
     * @param rClass 分组的第一级的返回类型类对象
     * @param <T>    数据的类型
     * @param <R>    顶级分组返回类型
     * @return 分组构造对象
     */
    public static <T, R> GroupBuilder<T, R> create(List<T> list, Class<R> rClass) {
        GroupBuilder<T, R> builder = new GroupBuilder<>();
        builder.groupData = list;
        builder.rclass = rClass;
        return builder;
    }

    /**
     * 根据提供的分组依据返回对应的分组规则，作为分组的主要手段
     *
     * @param sFun 提取分组值的lambda表达式，通常为getter的方法引用，如 Object::getField
     * @param <K>  分组值类型
     * @return 分组规则，可以详细的划分要如何分组，要提取哪些字段到父级分组 调用end方法结束这一次分组规则设置
     */
    public <K> GroupRules<T, K> groupBy(LambdaUtil.SerializableFunction<T, K> sFun) {
        GroupRules<T, K> tkGroupRules = new GroupRules<>(sFun);
        tkGroupRules.builder = this;
        return tkGroupRules;
    }

    /**
     * 构建分组结果，使用 Map 保存
     *
     * @return 分组后的集合
     */
    public Collection<Map<String, Object>> build() {
        GroupRules<T, ?> poll = groupQueue.poll();
        if (poll == null) {
            throw new IllegalStateException("未指定分组数据");
        }
        return build(poll, groupData);
    }

    /**
     * 根据单个分组规则构建结果的方法
     *
     * @param groupRules 分组规则
     * @param data       分组数据
     * @return 分组后的数据
     */
    @SuppressWarnings("unchecked")
    private Collection<Map<String, Object>> build(GroupRules<T, ?> groupRules, List<T> data) {
        if (groupRules == null || data == null) {
            throw new IllegalArgumentException("GroupRules or data cannot be null");
        }
        Map<Object, Map<String, Object>> map = new LinkedHashMap<>();
        for (T datum : data) {
            Object mainKey = groupRules.keyExt.apply(datum);
            String mainName = LambdaUtil.getName(groupRules.keyExt);
            Map<String, Object> stringObjectMap = map.get(mainKey);
            if (stringObjectMap == null) {
                Map<String, Object> g = new LinkedHashMap<>();
                g.put(mainName, mainKey);
                for (LambdaUtil.SerializableFunction<T, ?> otherExt : groupRules.ext) {
                    Object value = otherExt.apply(datum);
                    String key = LambdaUtil.getName(otherExt);
                    g.put(key, value);
                }
                if (groupRules.child.isEmpty()) {
                    List<Object> cgList = new ArrayList<>();
                    cgList.add(datum);
                    g.put(groupRules.childName, cgList);
                } else {
                    Map<String, Object> cg = new LinkedHashMap<>();
                    for (LambdaUtil.SerializableFunction<T, ?> c : groupRules.child) {
                        Object cValue = c.apply(datum);
                        String cKey = LambdaUtil.getName(c);
                        cg.put(cKey, cValue);
                    }
                    List<Object> cgList = new ArrayList<>();
                    cgList.add(cg);
                    g.put(groupRules.childName, cgList);
                }
                map.put(mainKey, g);
            } else {
                if (groupRules.child.isEmpty()) {
                    List<Object> cgList = (List<Object>) stringObjectMap.get(groupRules.childName);
                    cgList.add(datum);
                    stringObjectMap.put(groupRules.childName, cgList);
                } else {
                    Map<String, Object> cg = new LinkedHashMap<>();
                    for (LambdaUtil.SerializableFunction<T, ?> c : groupRules.child) {
                        Object cValue = c.apply(datum);
                        String cKey = LambdaUtil.getName(c);
                        cg.put(cKey, cValue);
                    }
                    List<Object> cgList = (List<Object>) stringObjectMap.get(groupRules.childName);
                    cgList.add(cg);
                    stringObjectMap.put(groupRules.childName, cgList);
                }
            }
        }
        Collection<Map<String, Object>> values = map.values();
        if (!groupQueue.isEmpty()) {
            GroupRules<T, ?> next = groupQueue.poll();
            for (Map<String, Object> value : values) {
                Object o = value.get(groupRules.childName);
                if (o instanceof List<?>) {
                    Collection<Map<String, Object>> build;
                    try {
                        build = build(next, (List<T>) o);
                    } catch (Exception e) {
                        throw new RuntimeException("请不要在最后一个分组前添加子元素的字段");
                    }
                    value.put(groupRules.childName, build);
                } else {
                    throw new IllegalStateException("Expected a List but found: " + o.getClass());
                }
            }
        }
        return values;
    }

    /**
     * 可以将分组数据转换为java对象的分组方法
     *
     * @param sFun         分组主要字段提取方法
     * @param targetClass  分组后的父类的类型
     * @param childClass   分组后子类的类型
     * @param <K>          分组主要字段的值类型
     * @param <ChildClass> 分组子类型
     * @param <E>          分组父类型
     * @return 分组Bean支持的分组规则
     */
    @SuppressWarnings("unchecked")
    public <K, ChildClass, E> GroupBenRules<T, K, E, ChildClass> groupBy(LambdaUtil.SerializableFunction<T, K> sFun, Class<E> targetClass, Class<ChildClass> childClass) {
        GroupBenRules<T, K, E, ChildClass> support = new GroupBenRules<>(sFun, targetClass, childClass);
        support.builder = (GroupBuilder<T, E>) this;
        return support;
    }

    /**
     * 顶级分组的父类分组方法
     *
     * @param sFun         分组主要字段提取方法
     * @param childClass   分组后子类的类型
     * @param <K>          分组主要字段的值类型
     * @param <ChildClass> 分组子类型
     * @return 分组Bean支持的分组规则
     */
    public <K, ChildClass> GroupBenRules<T, K, R, ChildClass> groupBy(LambdaUtil.SerializableFunction<T, K> sFun, Class<ChildClass> childClass) {
        return groupBy(sFun, this.rclass, childClass);
    }

    /**
     * 将原始数据构建为对应的分组数据bean对象
     *
     * @param <E> 分组指定类型
     * @return 分组后的数据
     */
    @SuppressWarnings("unchecked")
    public <E> Collection<E> buildToBean() {
        GroupBenRules<T, ?, ?, ?> first = groupQueueBean.poll();
        if (first == null) throw new IllegalArgumentException("未指定分组数据");
        return (Collection<E>) buildToBean(first, this.groupData);
    }

    /**
     * 构建分组对象集合
     *
     * @param support      分组规则
     * @param data         原始数据
     * @param <K>          分组主要字段的值类型
     * @param <E>          分组指定类型
     * @param <ChildClass> 分组子类型
     * @return 分组后的数据
     */
    @SuppressWarnings("unchecked")
    private <K, E, ChildClass> Collection<E> buildToBean(GroupBenRules<T, K, E, ChildClass> support, List<T> data) {
        if (support == null || data == null) {
            throw new IllegalArgumentException("GroupBenRules or data cannot be null");
        }

        Collection<E> result = new ArrayList<>();
        Map<Object, E> scale = new LinkedHashMap<>();

        for (T datum : data) {
            K mainKey = support.keyExt.apply(datum);
            E instance = scale.get(mainKey);

            if (instance == null) {
                instance = RefUtil.newInstance(support.targetClass);
                RefUtil.setField(instance, LambdaUtil.getFiledName(support.keyExt), mainKey, true);

                for (LambdaUtil.SerializableFunction<T, ?> otherField : support.ext) {
                    RefUtil.setField(instance, LambdaUtil.getFiledName(otherField), otherField.apply(datum), true);
                }

                if (support.child.isEmpty()) {
                    List<T> c = new ArrayList<>();
                    c.add(datum);
                    RefUtil.setField(instance, support.childName, c, true);
                } else {
                    List<ChildClass> cl = new ArrayList<>();
                    cl.add(createChildInstance(support, datum));
                    RefUtil.setField(instance, support.childName, cl, true);
                }

                result.add(instance);
                scale.put(mainKey, instance);
            } else {
                if (support.child.isEmpty()) {
                    List<T> o = (List<T>) RefUtil.get(RefUtil.getField(instance, support.childName), instance);
                    o.add(datum);
                } else {
                    List<ChildClass> childClasses = (List<ChildClass>) RefUtil.get(RefUtil.getField(instance, support.childName), instance);
                    childClasses.add(createChildInstance(support, datum));
                }
            }
        }

        if (!groupQueueBean.isEmpty()) {
            GroupBenRules<T, ?, ?, ?> next = groupQueueBean.poll();
            for (E instance : result) {
                Field field = RefUtil.getField(instance, support.childName);
                Object o = RefUtil.get(field, instance);
                if (o instanceof List<?>) {
                    try {
                        Collection<E> buildRes = (Collection<E>) buildToBean(next, (List<T>) o);
                        RefUtil.setField(instance, support.childName, buildRes, true);
                    } catch (Exception e) {
                        throw new RuntimeException("请不要在最后一个分组前添加子元素的字段");
                    }
                }
            }
        }

        return result;
    }

    private <K, E, ChildClass> ChildClass createChildInstance(GroupBenRules<T, K, E, ChildClass> support, T datum) {
        ChildClass childInstance = RefUtil.newInstance(support.childClass);
        for (LambdaUtil.SerializableFunction<T, ?> csf : support.child) {
            RefUtil.setField(childInstance, LambdaUtil.getFiledName(csf), csf.apply(datum), true);
        }
        return childInstance;
    }

/*    private <K, E, ChildClass> Collection<E> buildToBean(GroupBenRules<T, K, E, ChildClass> support, List<T> data) {
        if (support == null || data == null) {
            throw new IllegalArgumentException("GroupBenRules or data cannot be null");
        }
        Collection<E> result = new ArrayList<>();
        Map<Object, E> scale = new LinkedHashMap<>();
        for (T datum : data) {
            // 提取分类属性的值和名称
            E instance = RefUtil.newInstance(support.targetClass);
            K mainKey = support.keyExt.apply(datum);
            String mainName = LambdaUtil.getName(support.keyExt);
            E e = scale.get(mainKey);
            if (e == null) {
                RefUtil.setField(instance, mainName, mainKey, true);
                // 提取并设置父类的属性值
                for (LambdaUtil.SerializableFunction<T, ?> otherField : support.ext) {
                    Object value = otherField.apply(datum);
                    String name = LambdaUtil.getName(otherField);
                    RefUtil.setField(instance, name, value, true);
                }

                // 如果不是自定义的子类字段，将原本的数据设置进去
                if (support.child.isEmpty()) {
                    List<T> c = new ArrayList<>();
                    c.add(datum);
                    RefUtil.setField(instance, support.childName, c, true);
                }
                // 存在自定义的子类字段提取
                else {
                    ChildClass childInstance = RefUtil.newInstance(support.childClass);
                    for (LambdaUtil.SerializableFunction<T, ?> csf : support.child) {
                        Object value = csf.apply(datum);
                        String name = LambdaUtil.getName(csf);
                        RefUtil.setField(childInstance, name, value, true);
                    }
                    List<ChildClass> cl = new ArrayList<>();
                    cl.add(childInstance);
                    RefUtil.setField(instance, support.childName, cl, true);
                }

                result.add(instance);
                scale.put(mainKey, instance);
            } else {
                if (support.child.isEmpty()) {
                    Field field = RefUtil.getField(e, support.childName);
                    List<T> o = (List<T>) RefUtil.get(field, e);
                    o.add(datum);
                    RefUtil.setField(e, support.childName, o, true);
                } else {
                    ChildClass childInstance = RefUtil.newInstance(support.childClass);
                    for (LambdaUtil.SerializableFunction<T, ?> csf : support.child) {
                        Object value = csf.apply(datum);
                        String name = LambdaUtil.getName(csf);
                        RefUtil.setField(childInstance, name, value, true);
                    }
                    Field field = RefUtil.getField(e, support.childName);
                    List<ChildClass> childClasses = (List<ChildClass>) RefUtil.get(field, e);
                    childClasses.add(childInstance);
                    RefUtil.setField(e, support.childName, childClasses, true);
                }
            }

        }

        if (!groupQueueBean.isEmpty()) {
            GroupBenRules<T, ?, ?, ?> next = groupQueueBean.poll();
            for (E instance : result) {
                Field field = RefUtil.getField(instance, support.childName);
                Object o = RefUtil.get(field, instance);
                if (o instanceof List<?>) {
                    Collection<E> buildRes;
                    try {
                        buildRes = (Collection<E>) buildToBean(next, (List<T>) o);
                    } catch (Exception e) {
                        throw new RuntimeException("请不要在最后一个分组前添加子元素的字段");
                    }
                    RefUtil.setField(instance, support.childName, buildRes, true);
                }
            }
        }
        return result;
    }*/
}
