package com.wyc21.entity;

import java.util.List;
import lombok.Data;

@Data
public class PageResult<T> {
    private List<T> list; // 当前页数据列表
    private long total; // 总记录数
    private int pageNum; // 当前页码
    private int pageSize; // 每页大小
    private int pages; // 总页数
    

    public PageResult(List<T> list, long total, int pageNum, int pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) ((total + pageSize - 1) / pageSize); // 计算总页数
    }
}