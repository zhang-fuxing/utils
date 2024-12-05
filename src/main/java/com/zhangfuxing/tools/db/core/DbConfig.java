package com.zhangfuxing.tools.db.core;


/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/2
 * @email zhangfuxing1010@163.com
 */
public class DbConfig {
    private String driverClassName = "";
    private String url = "";
    private String username = "";
    private String password = "";
    private String libs = "";

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLibs() {
        return libs;
    }

    public void setLibs(String libs) {
        this.libs = libs;
    }

    @Override
    public String toString() {
        return "AppConfig{" +
               "driverClassName='" + driverClassName + '\'' +
               ", url='" + url + '\'' +
               ", username='" + username + '\'' +
               ", password='" + password + '\'' +
               ", libs='" + libs + '\'' +
               '}';
    }
}
