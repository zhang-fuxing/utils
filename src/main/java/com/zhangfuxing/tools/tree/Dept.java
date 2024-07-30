package com.zhangfuxing.tools.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/7/25
 * @email zhangfuxing1010@163.com
 */
public class Dept implements TreeSupport<Dept> {
    private Integer deptId;
    private Integer parentId;
    private String name;
    private Integer count;
    private List<Dept> child;

    @Override
    public void setId(Serializable id) {
        this.deptId = Integer.parseInt(String.valueOf(id));
    }

    @Override
    public Serializable getId() {
        return deptId;
    }

    @Override
    public void setParentId(Serializable parentId) {
        this.parentId = Integer.parseInt(String.valueOf(parentId));
    }

    @Override
    public Serializable getParentId() {
        return parentId;
    }

    @Override
    public List<Dept> getChild() {
        return this.child;
    }

    @Override
    public void addChild(Dept item) {
        if (this.child == null) {
            this.child = new ArrayList<>();
        }
        this.child.add(item);
    }

    @Override
    public Serializable getRootParentId() {
        return -1;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void setChild(List<Dept> child) {
        this.child = child;
    }

    @Override
    public String toString() {
        return "Dept{" +
               "deptId=" + deptId +
               ", parentId=" + parentId +
               ", name='" + name + '\'' +
               ", count=" + count +
               ", child=" + child +
               '}';
    }
}
