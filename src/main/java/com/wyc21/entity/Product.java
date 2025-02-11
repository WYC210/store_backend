package com.wyc21.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Product {
    private Long productId;          // 商品ID
    private String name;             // 商品名称
    private String description;      // 商品描述
    private BigDecimal price;        // 商品价格
    private Integer stock;           // 库存
    private Long categoryId;         // 分类ID
    private String brand;            // 品牌
    private String tags;             // 标签
    private String imageUrl;         // 主图URL
    private BigDecimal rating;       // 评分
    private Integer reviewCount;     // 评论数
    private Integer isActive;        // 商品状态：1-上架，0-下架
    private String createdUser;      // 创建者
    private LocalDateTime createdTime;    // 创建时间
    private String modifiedUser;     // 修改者
    private LocalDateTime modifiedTime;   // 修改时间

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }
}