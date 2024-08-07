package com.zhangfuxing.tools;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhangfuxing.tools.file.YmlGen;
import com.zhangfuxing.tools.group.GroupBuilder;
import com.zhangfuxing.tools.spring.ioc.Spring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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

    public static void main(String[] args) throws IOException {
        JSONObject entries = JSONUtil.parseObj(var);
        new YmlGen().create(entries).toFile(new File("./app.yml"));
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

    static String var = """
            {
              "spring": {
                "main": {
                  "lazy-initialization": true
                },
                "rabbitmq": {
                  "host": "192.192.9.42",
                  "port": 5672,
                  "username": "kssuser",
                  "password": "ksspasswjs",
                  "queue-prefix": ""
                },
                "datasource": {
                  "driver-class-name": "com.kingbase8.Driver",
                  "type": "com.alibaba.druid.pool.DruidDataSource",
                  "dynamic": {
                    "primary": "master",
                    "strict": true,
                    "master": {
                      "url": "jdbc:kingbase8://${host.datasource}:54321/basedb",
                      "username": "dbo",
                      "password": "ENC(xBWp1XP6TtGfXiETHsgUJQlx2BiRnlxcvR2XYlKNrrgXYC/zBQwrQhp8R6y3Zaii)"
                    },
                    "slave1": {
                      "url": "jdbc:kingbase8://${host.datasource}:54321/flowdata",
                      "username": "dbo",
                      "password": "ENC(xBWp1XP6TtGfXiETHsgUJQlx2BiRnlxcvR2XYlKNrrgXYC/zBQwrQhp8R6y3Zaii)"
                    }
                  },
                  "druid": {
                    "name": "springboot-hibernate",
                    "initial-size": 5,
                    "min-idle": 5,
                    "max-active": 30,
                    "max-wait": 60000,
                    "min-evictable-idle-time-millis": 300000,
                    "time-between-eviction-runs-millis": 60000,
                    "validation-query": "SELECT 1",
                    "test-on-borrow": true,
                    "test-on-return": true,
                    "test-while-idle": true,
                    "poolPreparedStatements": true,
                    "filters": "stat",
                    "keepAlive": true,
                    "maxPoolPreparedStatementPerConnectionSize": 20,
                    "useGlobalDataSourceStat": true,
                    "connectionProperties": "druid.stat.mergeSql=true;druid.stat.slowSqlMillis=500",
                    "stat-view-servlet": {
                      "enabled": true,
                      "allow": "127.0.0.1",
                      "login-username": "admin",
                      "login-password": "admin"
                    },
                    "filter": {
                      "stat": {
                        "enabled": true,
                        "log-slow-sql": true,
                        "slow-sql-millis": 3000,
                        "merge-sql": false
                      }
                    }
                  }
                },
                "servlet": {
                  "multipart": {
                    "max-file-size": "10GB",
                    "max-request-size": "10GB",
                    "location": "D:/workspace/service/temp"
                  }
                },
                "redis": {
                  "database": 0,
                  "host": "127.0.0.1",
                  "port": 6379,
                  "password": "ENC(wNhKjpVk/LzoHWsYp0+fxazvI1cTsmtCjZSrMVkjfGOeK3JncA21AkYKYBKZLnte)",
                  "expire": 7200
                }
              },
              "geologic": {
                "mail": {
                  "host": "cmail.cnooc.com.cn",
                  "port": 25,
                  "from": "sy_yunzhou<sy_yunzhou@cnooc.com.cn>",
                  "user": "sy_yunzhou@cnooc.com.cn",
                  "auth": true,
                  "pass": "R#4iojdklv7dhx",
                  "ssl": true
                },
                "constant": {
                  "webroot": "https://${host.server-host}:9900/",
                  "server-address": "https://${host.server-host}:${server.port}/",
                  "removeBakPath": "/hwdata/kingshine/removeBakPath",
                  "split-file-location": "/hwdata1/split_buffer",
                  "gis-conf-location": "./config/coord-converter-conf.json",
                  "session-id-Name": "SK",
                  "pluginOk": {
                    "matchPluginokNetSegmentNum": 0,
                    "pluginokServerIpPort1": "${host.plugin-address1}",
                    "pluginokClientSocketPort": 89
                  },
                  "docServersConf": {
                    "192.168.235.110": "http://$(host):8081",
                    "10.78.78.81": "http://$(host):9001"
                  },
                  "ftp": {
                    "username": "kingshine",
                    "password": "ENC(Sc62rPNhLFWz0cEoTk+dZFZ7xHd6NFkGuhVMd6qyllmtYKIXBmeyfH2Es0YnLPtY)",
                    "prefix": "http://",
                    "urlCall": "/handout/ftp_current_dir",
                    "urlCall0": "/handout/ftp_child_dir",
                    "service": "${host.ftp-publish-address}"
                  },
                  "databaseType": 1,
                  "datasources": [
                    "basedb",
                    "dev"
                  ],
                  "userSessionKey": "user",
                  "cookieName": "sessionId",
                  "prefix": "spring:session:sessions:expires:",
                  "sessionKey": "spring:session:sessions:",
                  "hashKey": "sessionAttr:",
                  "matchNetSegmentNum": 0,
                  "tempFileDirectory": "${host.workspace}/tempFile/",
                  "loginSystemName": "物探工程基础数据管理系统",
                  "remove-bak-path": "${host.workspace}/rmbak",
                  "redirectDownloadPath": "/api/dataMana/downloadDataItem",
                  "fileStreamPath": "/api/dataMana/fileStream?filePath="
                },
                "captchaImg": {
                  "captchaStr": "987654321QWERTYUIPASDFGHJKLZXCVBNM",
                  "captchaLen": 4,
                  "captchaWidth": 70,
                  "captchaHeight": 50
                },
                "file-transfer": {
                  "rootPath": "${host.workspace}/upload/",
                  "directory": {
                    "project": "project/",
                    "install": "installPackage/",
                    "doc": "doc/",
                    "tools": "tools/",
                    "archive": "archive/",
                    "dataOrder": "data-order/",
                    "update": "updatePackage/",
                    "tipArea": "tipArea/",
                    "scopeSearchTemp": "scopeSearchTemp/",
                    "video": "video/"
                  },
                  "packageNum": 3,
                  "packageInfo": [
                    {
                      "name": "64位安装包(用于9网段)",
                      "fileName": "Win10_NS9_x64",
                      "description": "适用于9网段中64位Win10系统使用的控件安装包"
                    },
                    {
                      "name": "64位安装包(用于10网段)",
                      "fileName": "Win10_NS10_x64",
                      "description": "适用于10网段中64位Win10系统使用的控件安装包"
                    },
                    {
                      "name": ".Net FrameWork 4.0安装包(仅用于Win7系统)",
                      "fileName": "Win7_NetFrameWork4_x64",
                      "description": "64位Win7系统中先安装.Net FrameWork 4.0，再安装控件包"
                    }
                  ],
                  "ftpConn": {
                    "host": "${host.ftp-upload-host}",
                    "port": 5010,
                    "username": "kingshine",
                    "password": "ENC(q5JUucSi5bIR1vTiWYQ2xKvqfRcpxHOksw/BLmjIzgrMB6DoxSKbv4ENhzZVjS11)"
                  }
                },
                "extension": {
                  "config": {
                    "map": {
                      "previewProxy": "192.168.235.40",
                      "onlyBrowseRole": false,
                      "enableAuthInc": true,
                      "addProjectCode": true,
                      "enableWebSocket": true,
                      "statCache": true,
                      "createDataGroup": true,
                      "initDataGroupMapping": true,
                      "dynamicDataSource": false,
                      "acceptFile": "xls, xlsx, csv, txt, doc, md, docx, pdf, ppt, pptx, zip, rar, 7z, gz, tar,jpg,jpeg,png,gif,bmp,webp,avi,mp4,rmvb,json,RPS,XPS,SPS"
                    },
                    "list-str": [
                      "ENC(5XP8v+vy99IS3cbYzBL3Fx86T+lh61ua55qylgCoarEEaSPNfr7MWI55KJbEPpcJ)"
                    ]
                  }
                },
                "enableModeration": false,
                "enableServerCheck": false,
                "isHistory": 0,
                "hibernate": {
                  "configLocation": "classpath:config/hibernate/kingbase8-cfg.xml",
                  "packagesToScan": "cn.com.kingshine.geologic"
                },
                "unit": "",
                "workgroup": "",
                "content1": "",
                "title": "",
                "templateDataType": "",
                "content2": ""
              }
            }
            """;
}
