package com.zhangfuxing.tools;

import com.zhangfuxing.tools.group.GroupBuilder;
import com.zhangfuxing.tools.spring.ioc.Spring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/22
 * @email zhangfuxing1010@163.com
 */
@Spring
public class MainClass {
    static Logger logger = LoggerFactory.getLogger(MainClass.class);

    public static void main(String[] args) throws IOException, InterruptedException {
    }


    static void testGroup() throws IOException {
        List<GT> list = List.of(
                G(1, 12, 0, "ce"),
                G(2, 33, 1, "ww"),
                G(1, 22, 0, "ww1"),
                G(1, 202, 1, "w0"),
                G(2, 303, 1, "2#"),
                G(2, 3, 1, "2aa")
        );
        Collection<GtId> objects = GroupBuilder.create(list, GtId.class)
                .groupBy(GT::getId, GtCh.class)
                .mainField(GT::getName)
                .setChildName(GtId::getGtchs)
                .end()
                .groupBy(GT::getType, GtCh.class, GtDesc.class)
                .setChildName(GtCh::getDesc)
                .childFiled(GT::getDesc)
                .end()
                .buildToBean();
        System.out.println(objects);
    }

    static GT G(Integer id, Integer name, Integer type, String desc) {
        return new GT(id, name, type, desc);
    }

    static class GT implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
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

    static class GtId {
        Integer id;
        Integer name;
        List<GtCh> gtchs;

        public List<GtCh> getGtchs() {
            return gtchs;
        }

        public void setGtchs(List<GtCh> gtchs) {
            this.gtchs = gtchs;
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

        @Override
        public String toString() {
            return "GtId{" +
                   "id=" + id +
                   ", name=" + name +
                   ", gtchs=" + gtchs +
                   '}';
        }
    }

    static class GtCh {
        Integer type;
        List<GtDesc> desc;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public List<GtDesc> getDesc() {
            return desc;
        }

        public void setDesc(List<GtDesc> desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "GtCh{" +
                   "type=" + type +
                   ", desc=" + desc +
                   '}';
        }
    }

    static class GtDesc {
        private String desc;

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "GtDesc{" +
                   "desc='" + desc + '\'' +
                   '}';
        }
    }

}
