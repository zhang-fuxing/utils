package com.zhangfuxing.tools.exception;

/**
 * @author zhangfx
 * @date 2023/4/20
 */
public class IllegalFieldException extends RuntimeException{
	public IllegalFieldException() {
	}
	
	public IllegalFieldException(String message) {
		super(message);
	}
}
