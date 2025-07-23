package com.zhangfuxing.tools.net;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/4/28
 * @email zhangfuxing1010@163.com
 */
public class URLBuilder {
	public static final String HTTP = "http";
	public static final String HTTPS = "https";
	public static final String WEBSOCKET = "ws";

	private String schema;
	private String host;
	private int port;
	private String path;
	private Map<String, String> queries;

	private URLBuilder() {
	}

	public static URLBuilder of(URI uri, boolean autoPort) {
		if (uri == null) {
			return new URLBuilder();
		}
		try {
			return of(uri.toURL(), autoPort);
		} catch (MalformedURLException e) {
			return newInstance(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), autoPort);
		}
	}

	private static URLBuilder newInstance(String schema, String host, int port, String path, String queryParams, boolean autoPort) {
		URLBuilder urlBuilder = new URLBuilder();
		urlBuilder.setSchema(schema);
		urlBuilder.setHost(host);
		urlBuilder.setPath(path);
		if (queryParams != null) {
			String[] split = queryParams.split("&");
			urlBuilder.queries = new LinkedHashMap<>(split.length);
			for (String s : split) {
				String[] split1 = s.split("=");
				urlBuilder.queries.put(split1[0], split1[1]);
			}
		}


		if (port <= 0 && HTTPS.equals(schema) && autoPort) {
			port = 443;
		} else if (port <= 0 && HTTP.equals(schema) && autoPort) {
			port = 80;
		}
		urlBuilder.setPort(port);

		return urlBuilder;
	}

	public static URLBuilder of(URL url, boolean autoPort) {
		URLBuilder urlBuilder = new URLBuilder();
		if (url != null) {
			urlBuilder = newInstance(url.getProtocol(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), autoPort);
		}
		return urlBuilder;
	}

	public static URLBuilder of(String url, boolean autoPort) {
		return of(URI.create(url), autoPort);
	}

	public static URLBuilder of(String url) {
		return of(url, true);
	}

	public static URLBuilder of(URL url) {
		return of(url, true);
	}

	public static URLBuilder of(URI uri) {
		return of(uri, true);
	}

	public static URLBuilder createURL() {
		return new URLBuilder();
	}

	public String schema() {
		return schema;
	}

	public URLBuilder setSchema(String schema) {
		this.schema = schema;
		return this;
	}

	public String host() {
		return host;
	}

	public URLBuilder setHost(String host) {
		this.host = host;
		return this;
	}

	public int port() {
		return port;
	}

	public URLBuilder setPort(int port) {
		this.port = port;
		return this;
	}

	public String path() {
		return path;
	}

	public URLBuilder setPath(String path) {
		this.path = path;
		return this;
	}

	public Map<String, String> queries() {
		return queries;
	}

	public URLBuilder setQueries(Map<String, String> queries) {
		this.queries = queries;
		return this;
	}

	public URLBuilder addQuery(String key, String value) {
		if (queries == null) {
			queries = new LinkedHashMap<>();
		}
		queries.put(key, value);
		return this;
	}

	public String build() {
		return schema + "://" + getHostAndPort() + (path == null || path.isBlank() ? "" : path) + getQuery();
	}

	public URL getURL() {
		try {
			return getURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public URI getURI() {
		return URI.create(this.build());
	}

	public String getQuery() {
		if (queries == null || queries.isEmpty()) {
			return "";
		}

		StringJoiner joiner = new StringJoiner("&", "?", "");
		for (Map.Entry<String, String> entry : queries.entrySet()) {
			String key = entry.getKey();
			String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
			joiner.add(key + "=" + value);
		}
		return joiner.toString();
	}

	private String getHostAndPort() {
		if (port <= 0) {
			return host;
		} else if (port == 443 && HTTPS.equals(schema)) {
			return host;
		} else if (port == 80 && HTTP.equals(schema)) {
			return host;
		} else {
			return host + ":" + port;
		}
	}

	@Override
	public String toString() {
		return "URLBuilder{" +
			   "schema='" + schema + '\'' +
			   ", host='" + host + '\'' +
			   ", port=" + port +
			   ", path='" + path + '\'' +
			   ", queries=" + queries +
			   '}';
	}
}
