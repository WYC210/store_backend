package com.wyc21.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ProductReview {
    private Long reviewId;
    private Long productId;
    private Long userId;
    private String username; // 添加用户名，用于显示
    private BigDecimal rating;
    private String content;
    private Date createdTime;
}