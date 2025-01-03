package com.zhangfuxing.tools.db.console;

import com.zhangfuxing.tools.db.core.DbDriver;
import com.zhangfuxing.tools.db.core.JavaxDbSetResolve;
import com.zhangfuxing.tools.db.core.SqlExecer;
import com.zhangfuxing.tools.db.page.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsoleDbManager {
    private static final Logger log = LoggerFactory.getLogger(ConsoleDbManager.class);
    private SqlExecer sqlExecer;
    private final Scanner scanner = new Scanner(System.in);
    private String outputPath = "sql_output/";

    public static void main(String[] args) {
        new ConsoleDbManager().start();
    }

    public void start() {
        printWelcome();
        while (true) {
            try {
                printMenu();
                String choice = scanner.nextLine().trim();
                
                switch (choice.toLowerCase()) {
                    case "1" -> connectDatabase();
                    case "2" -> executeSql();
                    case "3" -> executePageQuery();
                    case "4" -> setOutputPath();
                    case "5" -> showConnectionInfo();
                    case "q", "quit", "exit" -> {
                        closeConnection();
                        return;
                    }
                    default -> System.out.println("无效的选择，请重试");
                }
            } catch (Exception e) {
                log.error("操作失败: {}", e.getMessage(), e);
                System.out.println("操作失败: " + e.getMessage());
            }
        }
    }

    private void printWelcome() {
        System.out.println("=================================");
        System.out.println("    欢迎使用数据库管理工具 v1.0    ");
        System.out.println("=================================");
    }

    private void printMenu() {
        System.out.println("\n+--------+------------------+--------+------------------+");
        System.out.println("| 选项   | 功能描述         | 选项   | 功能描述         |");
        System.out.println("+--------+------------------+--------+------------------+");
        System.out.println("| 1      | 连接数据库       | 2      | 执行SQL          |");
        System.out.println("| 3      | 分页查询         | 4      | 设置输出路径     |");
        System.out.println("| 5      | 显示连接信息     | q      | 退出系统         |");
        System.out.println("+--------+------------------+--------+------------------+");
        System.out.print("请输入选择: ");
    }

    private void connectDatabase() {
        System.out.println("\n请输入数据库连接信息：");
        System.out.print("驱动路径 (直接回车使用默认'./lib/'): ");
        String driverPath = scanner.nextLine().trim();
        driverPath = driverPath.isEmpty() ? "./lib/" : driverPath;

        System.out.print("驱动类名 (可选): ");
        String driverClass = scanner.nextLine().trim();

        System.out.print("JDBC URL: ");
        String url = scanner.nextLine().trim();

        System.out.print("用户名: ");
        String username = scanner.nextLine().trim();

        System.out.print("密码: ");
        String password = scanner.nextLine().trim();

        try {
            Connection connection = DbDriver.getConnection(driverPath, driverClass, url, username, password);
            connection.setAutoCommit(false);
            sqlExecer = new SqlExecer(new JavaxDbSetResolve(), connection);
            sqlExecer.setShowSQL(true);
            System.out.println("数据库连接成功！");
        } catch (Exception e) {
            log.error("连接失败: {}", e.getMessage(), e);
            System.out.println("连接失败: " + e.getMessage());
        }
    }

    private void executeSql() {
        checkConnection();
        executeWithMenu();
    }

    private void executeWithMenu() {
        System.out.print("\n请输入SQL语句 (以;结束): ");
        StringBuilder sql = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine().trim()).endsWith(";")) {
            sql.append(line).append(" ");
        }
        sql.append(line);

        try {
            if (sql.toString().toLowerCase().startsWith("select")) {
                List<Map<String, Object>> results = sqlExecer.queryMaps(sql.toString());
                displayResults(results);
                showSqlMenu(results);
            } else {
                int affected = sqlExecer.executeUpdate(sql.toString());
                System.out.println("执行成功，影响行数: " + affected);
                showUpdateMenu();
            }
        } catch (Exception e) {
            log.error("SQL执行失败: {}", e.getMessage(), e);
            System.out.println("SQL执行失败: " + e.getMessage());
        }
    }

    private void showUpdateMenu() {
        while (true) {
            System.out.println("\n+--------+------------------+--------+------------------+");
            System.out.println("| 选项   | 功能描述         | 选项   | 功能描述         |");
            System.out.println("+--------+------------------+--------+------------------+");
            System.out.println("| y      | 提交事务         | n      | 取消更新         |");
            System.out.println("| c      | 继续执行         | b      | 返回主菜单       |");
            System.out.println("+--------+------------------+--------+------------------+");
            System.out.print("请输入选择: ");

            String choice = scanner.nextLine().trim().toLowerCase();
            switch (choice) {
                case "y" -> {
                    try {
                        sqlExecer.getConnection().commit();
                        System.out.println("事务已提交");
                        return;
                    } catch (Exception e) {
                        log.error("提交事务失败: {}", e.getMessage(), e);
                        System.out.println("提交事务失败: " + e.getMessage());
                    }
                }
                case "n" -> {
                    try {
                        sqlExecer.getConnection().rollback();
                        System.out.println("已取消更新");
                        return;
                    } catch (Exception e) {
                        log.error("回滚事务失败: {}", e.getMessage(), e);
                        System.out.println("回滚事务失败: " + e.getMessage());
                    }
                }
                case "c" -> {
                    executeWithMenu();
                    return;
                }
                case "b" -> {
                    try {
                        sqlExecer.getConnection().rollback();
                        System.out.println("未提交的更新已取消");
                    } catch (Exception e) {
                        log.error("回滚事务失败: {}", e.getMessage(), e);
                    }
                    return;
                }
                default -> System.out.println("无效的选择，请重试");
            }
        }
    }

    private void executePageQuery() {
        checkConnection();
        System.out.print("\n请输入查询SQL: ");
        String sql = sqlResolve(scanner.nextLine().trim());
        
        System.out.print("页码: ");
        int pageNum = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.print("每页记录数: ");
        int pageSize = Integer.parseInt(scanner.nextLine().trim());

        executePageQueryWithNav(sql, pageNum, pageSize);
    }

    private void executePageQueryWithNav(String sql, int pageNum, int pageSize) {
        try {
            while (true) {
                PageResult<Map<String, Object>> result = sqlExecer.queryPage(sql, pageNum, pageSize);
                System.out.println("\n查询结果：");
                System.out.println("总记录数: " + result.getTotal());
                System.out.println("总页数: " + result.getPages());
                System.out.println("当前页: " + result.getPageNum());
                displayResults(result.getRecords());
                
                // 显示分页导航菜单
                System.out.println("\n+--------+------------------+--------+------------------+--------+------------------+");
                System.out.println("| 选项   | 功能描述         | 选项   | 功能描述         | 选项   | 功能描述         |");
                System.out.println("+--------+------------------+--------+------------------+--------+------------------+");
                System.out.println("| p      | 上一页           | n      | 下一页           | f      | 首页             |");
                System.out.println("| l      | 末页             | s      | 保存结果         | q      | 新查询           |");
                System.out.println("| b      | 返回主菜单       |        |                  |        |                  |");
                System.out.println("+--------+------------------+--------+------------------+--------+------------------+");
                System.out.print("请输入选择: ");
                
                String choice = scanner.nextLine().trim().toLowerCase();
                switch (choice) {
                    case "n" -> {
                        if (pageNum < result.getPages()) {
                            pageNum++;
                        } else {
                            System.out.println("已经是最后一页了");
                        }
                    }
                    case "p" -> {
                        if (pageNum > 1) {
                            pageNum--;
                        } else {
                            System.out.println("已经是第一页了");
                        }
                    }
                    case "f" -> pageNum = 1;
                    case "l" -> pageNum = (int) result.getPages();
                    case "s" -> saveResultsToFile(result.getRecords());
                    case "q" -> {
                        executePageQuery();
                        return;
                    }
                    case "b" -> {
                        return;
                    }
                    default -> System.out.println("无效的选择，请重试");
                }
            }
        } catch (Exception e) {
            log.error("分页查询失败: {}", e.getMessage(), e);
            System.out.println("分页查询失败: " + e.getMessage());
        }
    }

    private String sqlResolve(String sql) {
        if (sql.endsWith(";")) {
            return sql.substring(0, sql.length() - 1);
        }
        return sql;
    }

    private void displayResults(List<Map<String, Object>> results) {
        if (results == null || results.isEmpty()) {
            System.out.println("查询结果为空");
            return;
        }

        // 打印到控制台
        printResultsToConsole(results);
    }

    private void printResultsToConsole(List<Map<String, Object>> results) {
        // 获取所有列名
        List<String> columns = results.get(0).keySet().stream().toList();
        
        // 计算每列的最大宽度
        int[] columnWidths = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            columnWidths[i] = column.length();
            for (Map<String, Object> row : results) {
                String value = String.valueOf(row.get(column));
                columnWidths[i] = Math.max(columnWidths[i], value.length());
            }
        }

        // 打印表头
        printRowSeparator(columnWidths);
        for (int i = 0; i < columns.size(); i++) {
            System.out.printf("| %-" + columnWidths[i] + "s ", columns.get(i));
        }
        System.out.println("|");
        printRowSeparator(columnWidths);

        // 打印数据
        for (Map<String, Object> row : results) {
            for (int i = 0; i < columns.size(); i++) {
                String value = String.valueOf(row.get(columns.get(i)));
                System.out.printf("| %-" + columnWidths[i] + "s ", value);
            }
            System.out.println("|");
        }
        printRowSeparator(columnWidths);
    }

    private void printRowSeparator(int[] columnWidths) {
        for (int width : columnWidths) {
            System.out.print("+-");
            System.out.print("-".repeat(width));
            System.out.print("-");
        }
        System.out.println("+");
    }

    private void saveResultsToFile(List<Map<String, Object>> results) {
        try {
            File dir = new File(outputPath);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new RuntimeException("无法创建输出目录");
            }

            String filename = outputPath + "query_result_" + 
                            System.currentTimeMillis() + ".txt";
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                // 写入表头
                List<String> columns = results.get(0).keySet().stream().toList();
                writer.write(String.join("\t", columns));
                writer.newLine();

                // 写入数据
                for (Map<String, Object> row : results) {
                    writer.write(columns.stream()
                            .map(col -> String.valueOf(row.get(col)))
                            .reduce((a, b) -> a + "\t" + b)
                            .orElse(""));
                    writer.newLine();
                }
            }
            System.out.println("结果已保存到: " + filename);
        } catch (Exception e) {
            log.error("保存文件失败: {}", e.getMessage(), e);
            System.out.println("保存文件失败: " + e.getMessage());
        }
    }

    private void setOutputPath() {
        System.out.print("\n请输入结果输出路径 (当前: " + outputPath + "): ");
        String path = scanner.nextLine().trim();
        if (!path.isEmpty()) {
            if (!path.endsWith("/")) {
                path += "/";
            }
            outputPath = path;
            System.out.println("输出路径已更新为: " + outputPath);
        }
    }

    private void showConnectionInfo() {
        if (sqlExecer == null || sqlExecer.getConnection() == null) {
            System.out.println("当前未连接到数据库");
            return;
        }

        try {
            Connection conn = sqlExecer.getConnection();
            System.out.println("\n当前连接信息：");
            System.out.println("URL: " + conn.getMetaData().getURL());
            System.out.println("用户名: " + conn.getMetaData().getUserName());
            System.out.println("数据库产品名称: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("数据库版本: " + conn.getMetaData().getDatabaseProductVersion());
            System.out.println("驱动名称: " + conn.getMetaData().getDriverName());
            System.out.println("驱动版本: " + conn.getMetaData().getDriverVersion());
        } catch (Exception e) {
            log.error("获取连接信息失败: {}", e.getMessage(), e);
            System.out.println("获取连接信息失败: " + e.getMessage());
        }
    }

    private void checkConnection() {
        if (sqlExecer == null || sqlExecer.getConnection() == null) {
            throw new IllegalStateException("请先连接数据库");
        }
    }

    private void closeConnection() {
        if (sqlExecer != null) {
            sqlExecer.close();
        }
        scanner.close();
        System.out.println("\n感谢使用，再见！");
    }

    private void showSqlMenu(List<Map<String, Object>> results) {
        while (true) {
            System.out.println("\n+--------+------------------+--------+------------------+");
            System.out.println("| 选项   | 功能描述         | 选项   | 功能描述         |");
            System.out.println("+--------+------------------+--------+------------------+");
            System.out.println("| c      | 继续执行         | s      | 保存结果         |");
            System.out.println("| b      | 返回主菜单       |        |                  |");
            System.out.println("+--------+------------------+--------+------------------+");
            System.out.print("请输入选择: ");

            String choice = scanner.nextLine().trim().toLowerCase();
            switch (choice) {
                case "c" -> {
                    executeWithMenu();
                    return;
                }
                case "s" -> saveResultsToFile(results);
                case "b" -> {
                    return;
                }
                default -> System.out.println("无效的选择，请重试");
            }
        }
    }
} 