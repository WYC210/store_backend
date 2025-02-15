package com.wyc21.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CartItem {
    private Long cartItemId;
    private Long cartId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
    private String productName;
    private String createdUser;
    private Date createdTime;
    private Date modifiedTime;
    private String imageUrl; 
}