package com.wyc21.entity;

import lombok.Data;
import java.util.List;

@Data
public class Category extends BaseEntity {
    private Long categoryId;           // 分类ID
    private String name;               // 分类名称
    private Long parentCategoryId;     // 父分类ID
    private List<Category> children;   // 子分类列表
} 