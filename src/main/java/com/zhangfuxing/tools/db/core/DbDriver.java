package com.zhangfuxing.tools.db.core;


import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.stream.Stream;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/2
 * @email zhangfuxing1010@163.com
 */
public class DbDriver {
    final DbConfig dbConfig;
    Connection connection;
    Driver driver;

    private DbDriver() {
        dbConfig = new DbConfig();
    }

    public DbDriver(DbConfig dbConfig) throws Exception {
        this.dbConfig = dbConfig;
        init();
    }


    private void init() throws Exception {
        String libs = dbConfig.getLibs();
        if (libs != null && !libs.isEmpty()) {
            this.driver = loadDriverByLibs(libs, dbConfig.getUrl(), dbConfig.getDriverClassName());
        } else {
            this.driver = DriverManager.getDriver(dbConfig.getUrl());
            if (this.driver == null && dbConfig.getDriverClassName() != null && !dbConfig.getDriverClassName().isEmpty()) {
                this.driver = loadDriverByClassName(dbConfig.getDriverClassName(), DriverManager.class.getClassLoader());
            }
        }

    }

    private static Driver loadDriverByLibs(String libs, String url, String... driverClassNames) throws Exception {
        if (libs == null || libs.isEmpty()) {
            return DriverManager.getDriver(url);
        }

        Path path = Paths.get(libs);
        String[] urls;
        if (Files.isDirectory(path)) {
            try (Stream<Path> walk = Files.walk(path, 1)) {
                urls = walk.map(Path::toString)
                        .filter(p -> p.endsWith(".jar"))
                        .toArray(String[]::new);
            }

        } else {
            if (!libs.endsWith(".jar")) {
                Log.error("请指定有效的数据库依赖 xxx.jar", null);
                return null;
            }
            urls = new String[]{path.toString()};
        }

        URL[] urlArray = Arrays.stream(urls)
                .map(lib -> {
                    try {
                        return new URL("jar:file:" + lib + "!/");
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }).toArray(URL[]::new);
        URLClassLoader classLoader = new URLClassLoader(urlArray);
        ServiceLoader<Driver> services = ServiceLoader.load(Driver.class, classLoader);
        Iterator<Driver> iterator = services.iterator();
        Driver driver = null;
        while (iterator.hasNext()) {
            Driver item = iterator.next();
            if (item.acceptsURL(url)) {
                driver = item;
                break;
            }
        }

        if (driver == null) {
            for (String driverClassName : driverClassNames) {
                try {
                    if (driverClassName == null || driverClassName.isEmpty()) {
                        continue;
                    }
                    driver = loadDriverByClassName(driverClassName, classLoader);
                    break;
                } catch (Exception ignored) {
                }
            }
        }
        return driver;
    }

    private static Driver loadDriverByClassName(String driverClassName, ClassLoader classLoader) throws Exception {
        Driver driver;
        Class<?> driverClass = Class.forName(driverClassName, true, classLoader);
        Constructor<?> constructor = driverClass.getDeclaredConstructor();
        driver = (Driver) constructor.newInstance();
        return driver;
    }

    public Driver getDriver() {
        return driver;
    }

    public Connection getConnection() throws SQLException {
        Properties properties = new Properties();
        properties.put("user", dbConfig.getUsername());
        properties.put("password", dbConfig.getPassword());
        if (dbConfig.getDriverClassName() == null || dbConfig.getDriverClassName().isEmpty()) {
            dbConfig.setDriverClassName(driver.getClass().getName());
        }
        connection = driver.connect(dbConfig.getUrl(), properties);
        return connection;
    }

    public static Connection getConnection(String jarLibs, String driverClassName, String url, String username, String password) throws Exception {
        DbDriver dd = new DbDriver();
        dd.dbConfig.setLibs(jarLibs);
        dd.dbConfig.setDriverClassName(driverClassName);
        dd.dbConfig.setUrl(url);
        dd.dbConfig.setUsername(username);
        dd.dbConfig.setPassword(password);
        dd.init();
        return dd.getConnection();
    }

    public static Connection getConnection(String driverClassName, String url, String username, String password) throws Exception {
        return getConnection(null, driverClassName, url, username, password);
    }

    public static Connection getConnection(String url, String username, String password) throws Exception {
        return getConnection(null, null, url, username, password);
    }
}
