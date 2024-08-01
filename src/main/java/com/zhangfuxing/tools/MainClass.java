package com.zhangfuxing.tools;

import cn.hutool.json.JSONUtil;
import com.zhangfuxing.tools.group.GroupBuilder;
import com.zhangfuxing.tools.spi.SpiUtil;
import com.zhangfuxing.tools.spring.aop.IService;
import com.zhangfuxing.tools.spring.ioc.Spring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/22
 * @email zhangfuxing1010@163.com
 */
@Spring
public class MainClass {
    static Logger logger = LoggerFactory.getLogger(MainClass.class);

    public static void main(String[] args) {
        IService iService = SpiUtil.loadFirst(IService.class);
        iService.doSomething();

    }
    void testGroup() {
        List<GT> list = List.of(
                G(1, 12, 0, "ce"),
                G(2, 33, 1, "ww"),
                G(1, 22, 0, "ww1"),
                G(1, 202, 1, "w0"),
                G(2, 303, 1, "2#"),
                G(2, 3, 1, "2aa")
        );
        Collection<Map<String, Object>> build = GroupBuilder.create(list)
                .groupBy(GT::getId)
                .mainField(GT::getName)
                .setChildName(GT::getType)
                .end()
                .groupBy(GT::getType)
                .childFiled(GT::getDesc)
                .setChildName(GT::getDesc)
                .end()
                .build();
        System.out.println(JSONUtil.toJsonStr(build));
    }

    static GT G(Integer id, Integer name, Integer type, String desc) {
        return new GT(id, name, type, desc);
    }
    static class GT {
        Integer id;
        Integer name;
        Integer type;
        String desc;

        public GT(Integer id, Integer name, Integer type, String desc) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.desc = desc;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getName() {
            return name;
        }

        public void setName(Integer name) {
            this.name = name;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "GT{" +
                   "id=" + id +
                   ", name=" + name +
                   ", type=" + type +
                   ", desc='" + desc + '\'' +
                   '}';
        }
    }
}
