package com.wyc21.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Product extends BaseEntity {
    private String productId; // 改为String类型
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String brand;
    private String tags;
    private String imageUrl;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer isActive;
    private String createdUser; // 创建者
    private LocalDateTime createdTime; // 创建时间
    private String modifiedUser; // 修改者
    private LocalDateTime modifiedTime; // 修改时间
    private String categoryId; // 改为String类型

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }
}