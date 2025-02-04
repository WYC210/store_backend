package com.wyc21.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Long productId;
    private Integer quantity;
    private String productName;
    private BigDecimal price;
    private String imageUrl;
}