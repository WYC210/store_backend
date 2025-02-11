package com.wyc21.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItem {
    private Long orderItemId;
    private String orderId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private String createdUser;
    private LocalDateTime createdTime;
    private String modifiedUser;
    private LocalDateTime modifiedTime;
} 