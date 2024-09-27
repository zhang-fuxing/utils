package com.zhangfuxing.tools.db;

import com.alibaba.druid.pool.DruidDataSource;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/27
 * @email zhangfuxing1010@163.com
 */
public class URLDataSource extends DruidDataSource {
    private final String[] jarPaths;

    public URLDataSource(String[] jarPaths) {
        super();
        this.jarPaths = jarPaths;
        loadJar();
    }

    public URLDataSource(String driverClassName, String url, String username, String password, String[] jarPaths) {
        super();
        this.jarPaths = jarPaths;
        loadJar();
        super.setDriverClassName(driverClassName);
        super.setUrl(url);
        super.setUsername(username);
        super.setPassword(password);
    }

    private void loadJar() {
        if (jarPaths == null || jarPaths.length == 0) {
            throw new IllegalArgumentException("jarPaths is null or empty");
        }
        URL[] urls = new URL[jarPaths.length];
        for (int i = 0; i < jarPaths.length; i++) {
            try {
                urls[i] = new URL("jar:file:" + jarPaths[i] + "!/");
            } catch (Exception e) {
                throw new IllegalArgumentException("jar path is invalid: " + jarPaths[i], e);
            }
        }
        URLClassLoader classLoader = new URLClassLoader(urls);
        super.setDriverClassLoader(classLoader);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String driverClassName;
        private String url;
        private String username;
        private String password;
        private String[] jarPaths;

        public Builder setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setJarPaths(String[] jarPaths) {
            this.jarPaths = jarPaths;
            return this;
        }

        public URLDataSource build() {
            return new URLDataSource(driverClassName, url, username, password, jarPaths);
        }
    }

}
