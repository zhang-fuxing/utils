package com.zhangfuxing.tools.group;

import com.zhangfuxing.tools.lambda.LambdaUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/6
 * @email zhangfuxing1010@163.com
 */
public class GroupBenRules<T, K, E, ChildClass> {
    // 提取分组主要属性的提取函数
    LambdaUtil.SerializableFunction<T, K> keyExt;
    // 提取分组父级属性的函数列表
    List<LambdaUtil.SerializableFunction<T, ?>> ext;
    // 提取子级属性的函数列表
    List<LambdaUtil.SerializableFunction<T, ?>> child;
    // 子级的名称
    String childName = "child";
    // 当前使用的构建者对象
    GroupBuilder<T,E> builder;
    // 转换到目标类的类对象
    Class<E> targetClass;
    Class<ChildClass> childClass;

    public GroupBenRules() {
        ext = new ArrayList<>();
        child = new ArrayList<>();
    }

    public GroupBenRules(LambdaUtil.SerializableFunction<T, K> keyExt, Class<E> targetClass, Class<ChildClass> childClass) {
        this();
        this.keyExt = keyExt;
        this.targetClass = targetClass;
        this.childClass = childClass;
    }

    /**
     * 设置包含在父对象的数据字段
     *
     * @param extFun 提取函数，如 class::getter
     * @param <V>    值类型
     * @return 当前分组对象
     */
    public <V> GroupBenRules<T, K, E,ChildClass> mainField(LambdaUtil.SerializableFunction<T, V> extFun) {
        this.ext.add(extFun);
        return this;
    }

    /**
     * 分组后的子项包含的字段名，如果有多级分组，只能在最后一个分组里设置子项包含的字段，否则会导致前面的分组失败
     * 例：<br/>
     * new GroupBuilder<GT>() <br/>
     * .group(list) <br/>
     * .groupBy(GT::getId) <br/>
     * .mainField(GT::getName) <br/>
     * // 第一个分组结束 <br/>
     * .end() <br/>
     * .groupBy(GT::getType) <br/>
     * // 只有最后一个分组才能调用此方法设置包含项 <br/>
     * .childFiled(GT::getDesc) <br/>
     * // 最后一个分组结束 <br/>
     * .end() <br/>
     * .build(); <br/>
     *
     * @param extFun 字段提取函数，一般为方法引用，如 class::getter
     * @param <V>    返回的值类型
     * @return 当前的分组对象
     */
    public <V> GroupBenRules<T, K, E,ChildClass> childFiled(LambdaUtil.SerializableFunction<T, V> extFun) {
        this.child.add(extFun);
        return this;
    }

    /**
     * 设置子数据的 key
     *
     * @param childName 名称
     * @return 当前分组对
     */
    public GroupBenRules<T, K, E,ChildClass> setChildName(String childName) {
        this.childName = childName;
        return this;
    }

    public GroupBenRules<T, K, E,ChildClass> setChildName(LambdaUtil.SerializableFunction<E, ?> lambda) {
        this.childName = LambdaUtil.getFiledName(lambda);
        return this;
    }

    /**
     * 结束当前分组设置
     *
     * @return 分组构造者对象
     */
    public GroupBuilder<T,E> end() {
        builder.groupQueueBean.add(this);
        return builder;
    }
}
