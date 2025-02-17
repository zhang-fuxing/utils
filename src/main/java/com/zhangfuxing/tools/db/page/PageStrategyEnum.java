package com.zhangfuxing.tools.db.page;

import com.zhangfuxing.tools.db.core.SqlExecer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public enum PageStrategyEnum implements PageStrategy {
	MYSQL {
		@Override
		public boolean support(String dbType) {
			return "mysql".equalsIgnoreCase(dbType);
		}

		@Override
		public String buildPageSql(String sql, int offset, int limit) {
			return sql + " LIMIT " + limit + " OFFSET " + offset;
		}
	},

	ORACLE {
		@Override
		public boolean support(String dbType) {
			return "oracle".equalsIgnoreCase(dbType);
		}

		@Override
		public String buildPageSql(String sql, int offset, int limit) {
			String version = getDbVersion();

			if (compareVersion(version, "12.0.0.0") >= 0) {
				return sql + " OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY";
			}

			return "SELECT * FROM (SELECT a.*, ROWNUM rn FROM (" + sql +
				   ") a WHERE ROWNUM <= " + (offset + limit) +
				   ") WHERE rn > " + offset;
		}
	},

	POSTGRESQL {
		@Override
		public boolean support(String dbType) {
			return "postgresql".equalsIgnoreCase(dbType) ||
				   dbType.toLowerCase().contains("kingbase");
		}

		@Override
		public String buildPageSql(String sql, int offset, int limit) {
			return sql + " LIMIT " + limit + " OFFSET " + offset;
		}
	},

	SQLSERVER {
		@Override
		public boolean support(String dbType) {
			return "sqlserver".equalsIgnoreCase(dbType);
		}

		@Override
		public String buildPageSql(String sql, int offset, int limit) {
			String version = getDbVersion();

			if (compareVersion(version, "11.0.0.0") >= 0) {
				return sql + " ORDER BY (SELECT NULL) OFFSET " + offset +
					   " ROWS FETCH NEXT " + limit + " ROWS ONLY";
			}

			return "SELECT * FROM (SELECT ROW_NUMBER() OVER (ORDER BY (SELECT 0)) AS RowNum, * FROM (" +
				   sql + ") AS Results) AS PagedResults WHERE RowNum > " + offset +
				   " AND RowNum <= " + (offset + limit);
		}
	};

	private static final Logger log = LoggerFactory.getLogger(PageStrategyEnum.class);

	public static PageStrategy getStrategy(String dbType) {
		for (PageStrategyEnum strategy : values()) {
			if (strategy.support(dbType)) {
				return strategy;
			}
		}
		throw new UnsupportedOperationException("Unsupported database type: " + dbType);
	}

	/**
	 * 获取数据库版本
	 */
	protected String getDbVersion() {
		try {
			Connection conn = SqlExecer.getCurrentConnection();
			if (conn != null) {
				return conn.getMetaData().getDatabaseProductVersion();
			}
		} catch (SQLException e) {
			log.warn("Failed to get database version", e);
		}
		return "0.0.0.0";
	}

	/**
	 * 比较版本号
	 */
	protected int compareVersion(String version1, String version2) {
		return DbVersion.compareVersion(version1, version2);
	}
} 