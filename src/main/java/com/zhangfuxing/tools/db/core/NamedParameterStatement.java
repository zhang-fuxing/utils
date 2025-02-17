package com.zhangfuxing.tools.db.core;

import cn.hutool.core.util.ReflectUtil;

import javax.persistence.Column;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/1/17
 * @email zhangfuxing1010@163.com
 */
public class NamedParameterStatement implements AutoCloseable {
	private String query;
	private final Map<String, List<Integer>> indexMap = new HashMap<>();
	private final PreparedStatement ps;
	private List<Field> fieldList; // 新增字段
	private int maxChunkSize = 500;

	public NamedParameterStatement(Connection connection, String query) throws SQLException {
		this.query = query;
		String statement = parse(query);
		this.ps = connection.prepareStatement(statement);

	}

	public void setMaxChunkSize(int maxChunkSize) {
		this.maxChunkSize = maxChunkSize;
	}

	public <T> void setParameters(T entity) throws SQLException {
		if (fieldList == null) {
			fieldList = Arrays.stream(ReflectUtil.getFields(entity.getClass()))
					.filter(field -> field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Id.class))
					.toList(); // 初始化 fieldList
		}
		JavaxDbSetResolve resolve = new JavaxDbSetResolve();
		for (Field field : fieldList) { // 使用已初始化的 fieldList
			field.setAccessible(true);
			String columnName = resolve.getColumnName(field);
			if (this.query.contains(":" + columnName)) {
				Object fieldValue = ReflectUtil.getFieldValue(entity, field);
				if (fieldValue instanceof Collection<?> collection) {
					if (collection.size() > 500) {
						List<?> subList = new ArrayList<>(collection);
						List<List<?>> splitLists = splitInClause(subList, maxChunkSize);
						for (List<?> splitList : splitLists) {
							String inClause = String.join(",", Collections.nCopies(splitList.size(), "?"));
							String newQuery = this.query.replaceFirst(":" + columnName, "(" + inClause + ")");
							NamedParameterStatement newStmt = new NamedParameterStatement(ps.getConnection(), newQuery);
							newStmt.setParameters(splitList);
							newStmt.getPreparedStatement().execute();
						}
					} else {
						setObject(columnName, fieldValue);
					}
				} else {
					setObject(columnName, fieldValue);
				}
			}
		}
	}

	public <T> void setParameters(Map<String, Object> params) throws SQLException {
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Collection<?> collection) {
				if (collection.size() > 500) {
					List<?> subList = new ArrayList<>(collection);
					List<List<?>> splitLists = splitInClause(subList, maxChunkSize);
					for (List<?> splitList : splitLists) {
						String inClause = String.join(",", Collections.nCopies(splitList.size(), "?"));
						String newQuery = this.query.replaceFirst(":" + key, "(" + inClause + ")");
						NamedParameterStatement newStmt = new NamedParameterStatement(ps.getConnection(), newQuery);
						Map<String, Object> splitParams = new HashMap<>();
						splitParams.put(key, splitList);
						newStmt.setParameters(splitParams);
						newStmt.getPreparedStatement().execute();
					}
				} else {
					setObject(key, value);
				}
			} else {
				setObject(key, value);
			}
		}
	}

	private String parse(String query) {
		int length = query.length();
		StringBuilder parsedQuery = new StringBuilder(length * 2); // 修改初始化长度
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		int index = 1;

		for (int i = 0; i < length; i++) {
			char c = query.charAt(i);
			if (inSingleQuote) {
				if (c == '\'') {
					inSingleQuote = false;
				}
			} else if (inDoubleQuote) {
				if (c == '"') {
					inDoubleQuote = false;
				}
			} else {
				if (c == '\'') {
					inSingleQuote = true;
				} else if (c == '"') {
					inDoubleQuote = true;
				} else if (c == ':' && i + 1 < length && Character.isJavaIdentifierStart(query.charAt(i + 1))) {
					int j = i + 2;
					while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
						j++;
					}
					String name = query.substring(i + 1, j);
					c = '?'; // Replace the named parameter with a positional parameter
					i += name.length(); // Move past the end of the named parameter

					List<Integer> indexList = indexMap.computeIfAbsent(name.toLowerCase(), k -> new ArrayList<>()); // 忽略大小写
					indexList.add(index);
					index++;
				}
			}
			parsedQuery.append(c);
		}
		return parsedQuery.toString();
	}

	private <T> List<List<?>> splitInClause(List<T> list, int maxChunkSize) {
		List<List<?>> chunks = new ArrayList<>();
		for (int i = 0; i < list.size(); i += maxChunkSize) {
			chunks.add(list.subList(i, Math.min(i + maxChunkSize, list.size())));
		}
		return chunks;
	}

	public void setObject(String name, Object value) throws SQLException {
		for (int i : getIndexes(name)) {
			ps.setObject(i, value);
		}
	}

	public void setInt(String name, int value) throws SQLException {
		for (int i : getIndexes(name)) {
			ps.setInt(i, value);
		}
	}

	public void setString(String name, String value) throws SQLException {
		for (int i : getIndexes(name)) {
			ps.setString(i, value);
		}
	}

	public void setLong(String name, long value) throws SQLException {
		for (int i : getIndexes(name)) {
			ps.setLong(i, value);
		}
	}

	public void setDouble(String name, double value) throws SQLException {
		for (int i : getIndexes(name)) {
			ps.setDouble(i, value);
		}
	}

	public void setFloat(String name, float value) throws SQLException {
		for (int i : getIndexes(name)) {
			ps.setFloat(i, value);
		}
	}

	public void setBoolean(String name, boolean value) throws SQLException {
		for (int i : getIndexes(name)) {
			ps.setBoolean(i, value);
		}
	}

	public void setBytes(String name, byte[] value) throws SQLException {
		for (int i : getIndexes(name)) {
			ps.setBytes(i, value);
		}
	}

	public void setNull(String name, int sqlType) throws SQLException {
		for (int i : getIndexes(name)) {
			ps.setNull(i, sqlType);
		}
	}

	public void setDate(String name, java.sql.Date value) throws SQLException { // 新增方法
		for (int i : getIndexes(name.toLowerCase())) { // 忽略大小写
			ps.setDate(i, value);
		}
	}

	private List<Integer> getIndexes(String name) {
		List<Integer> indexes = indexMap.get(name.toLowerCase()); // 忽略大小写
		if (indexes == null) {
			throw new IllegalArgumentException("Parameter not found: " + name);
		}
		return indexes;
	}

	public PreparedStatement getPreparedStatement() {
		return ps;
	}

	@Override
	public void close() throws SQLException {
		ps.close();
	}
}
