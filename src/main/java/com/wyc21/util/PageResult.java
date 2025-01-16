package com.wyc21.util;

import java.util.List;

import lombok.Data;

@Data
public class PageResult<T> {
    private List<T> list;      // 数据列表
    private long total;        // 总记录数
    private int pages;         // 总页数
    private int pageNum;       // 当前页码
    private int pageSize;      // 每页大小
} 