package com.wyc21.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartItem {
    private Long cartItemId;
    private Long cartId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
    private String productName;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
}