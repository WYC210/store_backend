package com.wyc21.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartItem extends BaseEntity {
    private String cartItemId;
    private String cartId;
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    private String productName;
    private String imageUrl;
    private String orderStatus;

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }
}