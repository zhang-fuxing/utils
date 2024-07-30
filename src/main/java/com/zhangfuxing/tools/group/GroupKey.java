package com.zhangfuxing.tools.group;

import com.zhangfuxing.tools.lambda.LambdaUtil;
import com.zhangfuxing.tools.lambda.SFun;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/7/29
 * @email zhangfuxing1010@163.com
 */
public class GroupKey<T, K> {
    SFun<T, K> keyExt;
    List<SFun<T, ?>> ext;
    List<SFun<T, ?>> child;
    String childName = "child";
    GroupBuilder<T> builder;

    public GroupKey() {
        ext = new ArrayList<>();
        child = new ArrayList<>();
    }

    public GroupKey(SFun<T, K> sFun) {
        this.keyExt = sFun;
        this.ext = new ArrayList<>();
        this.child = new ArrayList<>();
    }

    /**
     * 设置包含在父对象的数据字段
     *
     * @param extFun 提取函数，如 class::getter
     * @param <V>    值类型
     * @return 当前分组对象
     */
    public <V> GroupKey<T, K> mainField(SFun<T, V> extFun) {
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
    public <V> GroupKey<T, K> childFiled(SFun<T, V> extFun) {
        this.child.add(extFun);
        return this;
    }

    /**
     * 设置子数据的 key
     *
     * @param childName 名称
     * @return 当前分组对
     */
    public GroupKey<T, K> setChildName(String childName) {
        this.childName = childName;
        return this;
    }

    public GroupKey<T, K> setChildName(SFun<T, ?> lambda) {
        this.childName = LambdaUtil.getName(lambda);
        return this;
    }

    /**
     * 结束当前分组设置
     *
     * @return 分组构造者对象
     */
    public GroupBuilder<T> end() {
        builder.groupQueue.add(this);
        return builder;
    }
}
