package com.wyc21.entity;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class Product extends BaseEntity {
    private Long productId;        // 商品ID
    private String name;           // 商品名称
    private String description;    // 商品描述
    private BigDecimal price;      // 商品价格
    private Integer stock;         // 库存
    private Long categoryId;       // 分类ID
    private String sku;            // 库存单位
    private String brand;          // 品牌
    private String tags;           // 标签
    private BigDecimal rating;     // 评分
    private Integer reviewCount;   // 评论数
    private Integer lowStockThreshold;  // 库存预警阈值
    private Integer isActive;      // 是否上架
    private String imageUrl;       // 主图URL
} 