package com.zhangfuxing.tools;

import java.util.List;

/**
 * 分页对象
 *
 * @author zhangfx
 * @date 2023/4/18
 */
public class IPage<T> {
	/**
	 * 页数索引，表示第几页
	 */
	private Integer index;
	
	/**
	 * 页面大小，表示一页由多少数据
	 */
	private Integer pageSize;
	
	/**
	 * 当前页数据
	 */
	private List<T> data;
	
	/**
	 * 页数合计，一共多少页数据
	 */
	private Integer pageCount;
	
	/**
	 * 数据总条数，表示数据库一共有多少符合条件的数据
	 */
	private Integer totalCount;
	
	public IPage() {
	}
	
	public IPage(List<T> data, Integer pageSize, Integer index, Integer totalCount) {
		this.pageSize = pageSize;
		this.data = data;
		this.totalCount = totalCount;
		this.index = index;
		this.pageCount = Math.toIntExact(OptionUtil.allPage(totalCount, pageSize));
	}
	
	public Integer getIndex() {
		return index;
	}
	
	public void setIndex(Integer index) {
		this.index = index;
	}
	
	public Integer getPageSize() {
		return pageSize;
	}
	
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	
	public List<T> getData() {
		return data;
	}
	
	public void setData(List<T> data) {
		this.data = data;
	}
	
	public Integer getPageCount() {
		return pageCount;
	}
	
	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}
	
	public Integer getTotalCount() {
		return totalCount;
	}
	
	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}
}
