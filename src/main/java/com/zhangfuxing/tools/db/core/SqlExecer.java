package com.zhangfuxing.tools.db.core;

import com.zhangfuxing.tools.db.page.PageResult;
import com.zhangfuxing.tools.db.page.PageStrategy;
import com.zhangfuxing.tools.db.page.PageStrategyEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
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
public class SqlExecer implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(SqlExecer.class);

	DbSetResolve resolve;
	Connection connection;
	boolean showSQL = false;
	private boolean autoClose = false;  // 是否自动关闭连接
	private int queryTimeout = 30;      // 查询超时时间(秒)
	private int fetchSize = 100;        // 每次获取的记录数

	private static final ThreadLocal<Connection> CURRENT_CONNECTION = new ThreadLocal<>();

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
				log.error("关闭连接失败: {}", e.getMessage());
			} finally {
				connection = null;
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
		if (showSQL) {
			log.info("Executing SQL: {}", sql);
		}

		// 先获取元数据信息，帮助调试
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			if (showSQL) {
				log.info("Result set metadata:");
				for (int i = 1; i <= columnCount; i++) {
					log.info("Column {}: name={}, label={}, type={}",
							i, metaData.getColumnName(i),
							metaData.getColumnLabel(i),
							metaData.getColumnTypeName(i));
				}
			}
		} catch (SQLException e) {
			log.error("Error getting metadata", e);
		}

		return executeQuery(sql, entityClass, hock, resolve::getEntities);
	}

	private <R> R executeQuery(String sql, Consumer<PreparedStatement> hock, Function<ResultSet, R> map) {
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		try {
			ps = connection.prepareStatement(sqlResolve(sql));
			final PreparedStatement finalPs = ps;
			ps.setQueryTimeout(queryTimeout);
			ps.setFetchSize(fetchSize);
			Optional.ofNullable(hock).ifPresent(h -> h.accept(finalPs));
			resultSet = ps.executeQuery();
			return map.apply(resultSet);
		} catch (SQLException e) {
			log.error("SQL执行失败: {}, 参数: {}", sql, e.getMessage());
			throw new RuntimeException("SQL执行失败: " + e.getMessage(), e);
		} finally {
			closeQuietly(resultSet);
			closeQuietly(ps);
			if (autoClose) {
				close();
			}
		}
	}

	private <T, R> R executeQuery(String sql, Class<T> entityClass, Consumer<PreparedStatement> hock, BiFunction<ResultSet, Class<T>, R> biMap) {
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		try {
			ps = connection.prepareStatement(sqlResolve(sql));
			ps.setQueryTimeout(queryTimeout);
			ps.setFetchSize(fetchSize);
			PreparedStatement finalPs = ps;
			Optional.ofNullable(hock).ifPresent(h -> h.accept(finalPs));
			resultSet = ps.executeQuery();
			return biMap.apply(resultSet, entityClass);
		} catch (SQLException e) {
			log.error("SQL执行失败: {}, 实体类: {}, 参数: {}", sql, entityClass.getName(), e.getMessage());
			throw new RuntimeException("SQL执行失败: " + e.getMessage(), e);
		} finally {
			closeQuietly(resultSet);
			closeQuietly(ps);
			if (autoClose) {
				close();
			}
		}
	}

	/**
	 * 安全关闭资源
	 */
	private void closeQuietly(AutoCloseable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				log.warn("关闭资源失败: {}", e.getMessage());
			}
		}
	}

	public int executeUpdate(String sql) {
		return executeUpdate(sql, null);
	}

	public int executeUpdate(String sql, Consumer<PreparedStatement> hock) {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sqlResolve(sql));
			ps.setQueryTimeout(queryTimeout);
			if (hock != null) {
				hock.accept(ps);
			}
			int result = ps.executeUpdate();
			if (showSQL) {
				log.info("更新影响行数: {}", result);
			}
			return result;
		} catch (SQLException e) {
			log.error("SQL执行失败: {}, 参数: {}", sql, e.getMessage());
			throw new RuntimeException("SQL执行失败: " + e.getMessage(), e);
		} finally {
			closeQuietly(ps);
			if (autoClose) {
				close();
			}
		}
	}

	/**
	 * 批量执行SQL
	 */
	public int[] executeBatch(String sql, List<Consumer<PreparedStatement>> hocks) {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sqlResolve(sql));
			ps.setQueryTimeout(queryTimeout);
			for (Consumer<PreparedStatement> hock : hocks) {
				hock.accept(ps);
				ps.addBatch();
			}
			return ps.executeBatch();
		} catch (SQLException e) {
			log.error("批量SQL执行失败: {}, 参数数量: {}", sql, hocks.size());
			throw new RuntimeException("批量SQL执行失败: " + e.getMessage(), e);
		} finally {
			closeQuietly(ps);
			if (autoClose) {
				close();
			}
		}
	}

	public void setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
	}

	public void setQueryTimeout(int queryTimeout) {
		this.queryTimeout = queryTimeout;
	}

	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
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

	/**
	 * 分页查询
	 */
	public <T> PageResult<T> queryPage(String sql, Class<T> entityClass, int pageNum, int pageSize) {
		return queryPage(sql, entityClass, pageNum, pageSize, null);
	}

	public <T> PageResult<T> queryPage(String sql, Class<T> entityClass, int pageNum, int pageSize, Consumer<PreparedStatement> hock) {
		// 先查询总数
		String countSql = "SELECT COUNT(*) FROM (" + sql + ") t";
		long total = queryCount(countSql, hock);

		// 设置 fetchSize 为页大小
		int originalFetchSize = this.fetchSize;
		this.fetchSize = pageSize;

		try {
			// 计算分页参数
			int offset = (pageNum - 1) * pageSize;
			String pageSql = buildPageSql(sql, offset, pageSize);

			// 执行分页查询
			List<T> records = queryEntities(pageSql, entityClass, hock);

			return new PageResult<>(
					pageNum,
					pageSize,
					total,
					(total + pageSize - 1) / pageSize,
					records
			);
		} finally {
			// 恢复原始 fetchSize
			this.fetchSize = originalFetchSize;
		}
	}

	public <T> PageResult<Map<String, Object>> queryPage(String sql, int pageNum, int pageSize) {
		return queryPage(sql, pageNum, pageSize, null);
	}

	public <T> PageResult<Map<String, Object>> queryPage(String sql, int pageNum, int pageSize, Consumer<PreparedStatement> hock) {
		// 先查询总数
		String countSql = "SELECT COUNT(*) FROM (" + sql + ") t";
		long total = queryCount(countSql, hock);

		// 设置 fetchSize 为页大小
		int originalFetchSize = this.fetchSize;
		this.fetchSize = pageSize;

		try {
			// 计算分页参数
			int offset = (pageNum - 1) * pageSize;
			String pageSql = buildPageSql(sql, offset, pageSize);

			// 执行分页查询
			List<Map<String, Object>> records = queryMaps(pageSql, hock);

			return new PageResult<>(
					pageNum,
					pageSize,
					total,
					(total + pageSize - 1) / pageSize,
					records
			);
		} finally {
			// 恢复原始 fetchSize
			this.fetchSize = originalFetchSize;
		}
	}

	private String buildPageSql(String sql, int offset, int limit) {
		CURRENT_CONNECTION.set(this.connection);
		try {
			PageStrategy strategy = PageStrategyEnum.getStrategy(getDbType());
			return strategy.buildPageSql(sql, offset, limit);
		} finally {
			CURRENT_CONNECTION.remove();
		}
	}

	private String getDbType() {
		try {
			return connection.getMetaData().getDatabaseProductName();
		} catch (SQLException e) {
			throw new RuntimeException("Failed to get database type", e);
		}
	}

	private long queryCount(String sql, Consumer<PreparedStatement> hock) {
		return Optional.ofNullable(queryMap(sql, hock))
				.map(map -> map.values().iterator().next())
				.map(value -> Long.parseLong(value.toString()))
				.orElse(0L);
	}

	public static Connection getCurrentConnection() {
		return CURRENT_CONNECTION.get();
	}

	/**
	 * 获取当前数据库连接
	 */
	public Connection getConnection() {
		return this.connection;
	}
}
