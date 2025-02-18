package com.wyc21.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItem {
    private String orderItemId;
    private String orderId;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private String createdUser;
    private LocalDateTime createdTime;
    private String modifiedUser;
    private LocalDateTime modifiedTime;
}