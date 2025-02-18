package com.wyc21.entity;

import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
public class Category extends BaseEntity {
    private String categoryId; // 确保是String类型
    private String name;
    private String parentId; // 确保是String类型
    private Integer level;
    private Integer sortOrder;
    private Boolean isActive;
    private List<Category> children; // 用于构建树形结构
    private String createdUser;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime; // 改为 modifiedTime 以匹配数据库字段
}