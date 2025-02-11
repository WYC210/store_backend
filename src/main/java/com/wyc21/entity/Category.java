package com.wyc21.entity;

import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
public class Category extends BaseEntity {
    private Long categoryId; // 分类ID
    private String name; // 分类名称
    private Long parentCategoryId; // 父类别ID
    private List<Category> children; // 子分类列表
    private LocalDateTime createdTime; // 创建时间
    private LocalDateTime modifiedTime; // 修改时间
}