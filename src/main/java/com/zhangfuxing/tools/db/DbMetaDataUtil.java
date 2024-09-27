package com.zhangfuxing.tools.db;

import com.zhangfuxing.tools.db.javax.ResultSetUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/27
 * @email zhangfuxing1010@163.com
 */
public class DbMetaDataUtil {

    public static TableMetaData getTableMetaData(DataSource dataSource, String schema, String tableName) {
        try (Connection connection = dataSource.getConnection()) {
            return getTableMetaData(connection, schema, tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static TableMetaData getTableMetaData(Connection connection, String schema, String tableName) {
        try {
            var dbMetaData = connection.getMetaData();
            ResultSet tables = dbMetaData.getTables(null, schema, tableName, new String[]{"TABLE"});
            List<TableMetaData> tableMetaDataList = ResultSetUtil.toList(tables, TableMetaData.class);
            tables.close();
            TableMetaData tableMetaData = tableMetaDataList.get(0);
            ResultSet columns = dbMetaData.getColumns(null, schema, tableName, null);
            List<ColumnMetaData> columnMetaDataList = ResultSetUtil.toList(columns, ColumnMetaData.class);
            columns.close();
            tableMetaData.setColumnMetaData(columnMetaDataList);
            return tableMetaData;
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
