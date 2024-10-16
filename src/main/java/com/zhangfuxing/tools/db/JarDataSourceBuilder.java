package com.zhangfuxing.tools.db;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/25
 * @email zhangfuxing@kingshine.com.cn
 */
public class JarDataSourceBuilder {
    private JarDataSource dataSource;

    public static JarDataSourceBuilder create() {
        JarDataSourceBuilder builder = new JarDataSourceBuilder();
        builder.dataSource = new JarDataSource();
        return builder;
    }

    public JarDataSourceBuilder jars(String... jarPaths) {
        this.dataSource.jarPaths = jarPaths;
        return this;
    }

    public JarDataSourceBuilder driverClassName(String driverClassName) {
        this.dataSource.driverClass = driverClassName;
        return this;
    }

    public JarDataSourceBuilder url(String jdbcUrl) {
        this.dataSource.url = jdbcUrl;
        return this;
    }
    public JarDataSourceBuilder username(String username) {
        this.dataSource.username = username;
        return this;
    }

    public JarDataSourceBuilder password(String password) {
        this.dataSource.password = password;
        return this;
    }

    public JarDataSource build() {
        return this.dataSource;
    }
}
