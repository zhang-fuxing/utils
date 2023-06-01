package com.zhangfuxing.tools.tools.record;

/**
 * @author zhangfx
 * @date 2023/4/18
 */
public record Page(Integer index, Integer pageSize, String sortField, String sortType) {
	
	public Page(Integer index, Integer pageSize) {
		this(index, pageSize, null, null);
	}
	
	public static Page defaultInstance() {
		return new Page(1, 2000);
	}
	
	public static Page defaultInstance(Integer pageSize) {
		return new Page(1, pageSize);
	}
	
	@Override
	public String toString() {
		return "Page{" +
				"index=" + index +
				", pageSize=" + pageSize +
				", sortField='" + sortField + '\'' +
				", sortType='" + sortType + '\'' +
				'}';
	}
}
