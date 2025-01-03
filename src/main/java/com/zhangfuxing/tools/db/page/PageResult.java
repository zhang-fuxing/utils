package com.zhangfuxing.tools.db.page;

import java.util.List;

public class PageResult<T> {
    private final int pageNum;      // 当前页码
    private final int pageSize;     // 每页大小
    private final long total;       // 总记录数
    private final long pages;       // 总页数
    private final List<T> records;  // 当前页数据

    public PageResult(int pageNum, int pageSize, long total, long pages, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = pages;
        this.records = records;
    }

    // Getters
    public int getPageNum() { return pageNum; }
    public int getPageSize() { return pageSize; }
    public long getTotal() { return total; }
    public long getPages() { return pages; }
    public List<T> getRecords() { return records; }
} 