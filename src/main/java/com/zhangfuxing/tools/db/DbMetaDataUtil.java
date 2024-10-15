package com.zhangfuxing.tools.db;

import com.zhangfuxing.tools.db.javax.ResultSetUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/27
 * @email zhangfuxing1010@163.com
 */
public class DbMetaDataUtil {


    public static Map<String, Object> getTableMetaData(final DataSource dataSource, final String schema, final String tableName, boolean containView) {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(tableName);
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, schema, tableName, containView ? new String[]{"TABLE", "VIEW"} : new String[]{"TABLE"});
            var tableMetaData = ResultSetUtil.toTableMetaData(tables);
            tables.close();
            if (tableMetaData.isEmpty()) {
                return null;
            }
            var result = tableMetaData.get(0);
            ResultSet columns = metaData.getColumns(null, schema, tableName, null);
            var columnMetaData = ResultSetUtil.toColumnMetaData(columns);
            columns.close();
            result.put("COLUMN_META_DATA", columnMetaData);
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, Object>> getAllContent(DataSource dataSource, String sql) {
        try (Connection connection = dataSource.getConnection()) {
            return getAllContent(connection, sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, Object>> getAllContent(Connection connection, String sql) {
        try {
            @SuppressWarnings("SqlSourceToSinkFlow")
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet resultSet = ps.executeQuery();
            return ResultSetUtil.toMap(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
