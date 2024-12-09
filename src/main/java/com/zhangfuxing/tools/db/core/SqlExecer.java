package com.zhangfuxing.tools.db.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/12/9
 * @email zhangfuxing1010@163.com
 */
@SuppressWarnings("SqlSourceToSinkFlow")
public class SqlExecer {
    private static final Logger log = LoggerFactory.getLogger(SqlExecer.class);

    DbSetResolve resolve;
    Connection connection;
    boolean showSQL = false;
    private SqlExecer() {
    }

    public SqlExecer(Connection connection) {
        this.connection = connection;
        this.resolve = new JavaxDbSetResolve();
    }

    public SqlExecer(DbSetResolve resolve, Connection connection) {
        this.resolve = resolve;
        this.connection = connection;
    }

    public void setShowSQL(boolean showSQL) {
        this.showSQL = showSQL;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                log.info("connection closed");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Map<String, Object> queryMap(String sql) {
        return executeQuery(sql, null, resolve::next);

    }

    public Map<String, Object> queryMap(String sql, Consumer<PreparedStatement> hock) {
        return executeQuery(sql, hock, resolve::next);
    }

    public List<Map<String, Object>> queryMaps(String sql) {
        return queryMaps(sql, null);
    }

    public List<Map<String, Object>> queryMaps(String sql, Consumer<PreparedStatement> hock) {
        return executeQuery(sql, hock, resolve::getMap);
    }

    public <T> T queryEntity(String sql, Class<T> entityClass) {
        return queryEntity(sql, entityClass, null);
    }

    public <T> T queryEntity(String sql, Class<T> entityClass, Consumer<PreparedStatement> hock) {
        return executeQuery(sql, entityClass, hock, resolve::next);

    }

    public <T> List<T> queryEntities(String sql, Class<T> entityClass) {
        return queryEntities(sql, entityClass, null);
    }

    public <T> List<T> queryEntities(String sql, Class<T> entityClass, Consumer<PreparedStatement> hock) {
        return executeQuery(sql, entityClass, hock, resolve::getEntities);
    }

    private <R> R executeQuery(String sql, Consumer<PreparedStatement> hock, Function<ResultSet, R> map) {
        try (PreparedStatement ps = connection.prepareStatement(sqlResolve(sql))) {
            Optional.ofNullable(hock).ifPresent(h -> h.accept(ps));
            try (ResultSet resultSet = ps.executeQuery()) {
                return map.apply(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T, R> R executeQuery(String sql, Class<T> entityClass, Consumer<PreparedStatement> hock, BiFunction<ResultSet, Class<T>, R> biMap) {
        try (PreparedStatement ps = connection.prepareStatement(sqlResolve(sql))) {
            Optional.ofNullable(hock).ifPresent(h -> h.accept(ps));
            try (ResultSet resultSet = ps.executeQuery()) {
                return biMap.apply(resultSet, entityClass);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public int executeUpdate(String sql) {
        return executeUpdate(sql, null);
    }

    public int executeUpdate(String sql, Consumer<PreparedStatement> hock) {
        try (PreparedStatement ps = connection.prepareStatement(sqlResolve(sql))) {
            Optional.ofNullable(hock).ifPresent(h -> h.accept(ps));
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String sqlResolve(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new RuntimeException("sql is null or empty");
        }
        sql = sql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
            return sqlResolve(sql);
        }
        if (showSQL) {
            log.info(sql);
        }
        return sql;
    }

}
