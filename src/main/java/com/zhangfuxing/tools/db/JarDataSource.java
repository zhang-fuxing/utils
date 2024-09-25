package com.zhangfuxing.tools.db;

import com.zhangfuxing.tools.classutil.ClassUtil;
import com.zhangfuxing.tools.spi.SpiUtil;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * 支持从外部jar包创建数据库连接的数据源实现；该数据源只能执行简单逻辑，如要实现其他复杂逻辑请更换其他可靠数据源
 * <p>
 * DruidDataSource 可以完美替换此类
 *
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/25
 * @email zhangfuxing@kingshine.com.cn
 */
public class JarDataSource implements DataSource {
    String[] jarPaths;
    String driverClass;
    String url;
    String username;
    String password;
    int loginTimeout = 30;

    private Collection<Driver> drivers;

    private volatile ClassLoader classLoader;
    private volatile boolean inited = false;

    protected PrintWriter logWriter = new PrintWriter(System.out);

    JarDataSource() {
    }

    public static JarDataSourceBuilder builder() {
        return JarDataSourceBuilder.create();
    }

    void init() {
        if (inited) return;
        inited = true;
        if (jarPaths != null && jarPaths.length != 0) {
            this.classLoader = ClassUtil.loadJar(jarPaths);
        } else {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (driverClass == null) {
            drivers = SpiUtil.loadAll(Driver.class, this.classLoader);
        } else {
            try {
                Class<?> aClass = ClassUtil.loadClass(driverClass, classLoader);
                Driver instance = (Driver) ClassUtil.getInstance(aClass);
                drivers = new ArrayList<>();
                drivers.add(instance);
            } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                     InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Connection connection(Properties properties) {
        init();
        for (Driver driver : drivers) {
            try {
                driver.acceptsURL(url);
                if (driver.acceptsURL(url)) {
                    return driver.connect(url, properties);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
        throw new RuntimeException("连接异常：无法获取数据库连接");
    }


    public void execSQL(Consumer<Connection> execAction) {
        try (Connection connection = getConnection()) {
            execAction.accept(connection);
        } catch (SQLException e) {
            throw new RuntimeException("sql执行失败");
        }
    }

    public List<Rows> execQuerySQL(String sql) {
        return execQuerySQL(sql, null);
    }

    public List<Rows> execQuerySQL(String sql, Consumer<PreparedStatement> setParamAction) {
        try (Connection connection = getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            if (setParamAction != null) {
                setParamAction.accept(ps);
            }
            ResultSet resultSet = ps.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<Rows> rows = new ArrayList<>();
            while (resultSet.next()) {
                List<Cols> cols = new ArrayList<>(0);
                for (int i = 0; i < columnCount; i++) {
                    var index = i + 1;
                    Cols col = new Cols(metaData.getColumnName(index), resultSet.getObject(index), metaData.getColumnClassName(index));
                    cols.add(col);
                }
                rows.add(new Rows(cols));
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("sql执行失败");
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        Properties properties = new Properties();
        properties.put("user", this.username);
        properties.put("password", this.password);
        return connection(properties);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Properties properties = new Properties();
        properties.put("user", username);
        properties.put("password", password);
        return connection(properties);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.logWriter = out;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return this.loginTimeout;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
