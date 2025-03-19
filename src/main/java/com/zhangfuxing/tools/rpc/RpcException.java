package com.zhangfuxing.tools.rpc;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/3/19
 * @email zhangfuxing1010@163.com
 */
public class RpcException extends RuntimeException {

	int code;
	String url;

	public RpcException() {
	}

	public RpcException(String message) {
		super(message);
	}

	public RpcException(String message, Throwable cause) {
		super(message, cause);
	}

	public RpcException(Throwable cause) {
		super(cause);
	}

	public RpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RpcException(String message, int code, String url) {
		super(message);
		this.code = code;
		this.url = url;
	}


	public RpcException(String message, int code, String url, Throwable ex) {
		super(message, ex);
		this.code = code;
		this.url = url;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
