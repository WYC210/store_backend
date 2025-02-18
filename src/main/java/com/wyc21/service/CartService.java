package com.wyc21.service;

import com.wyc21.dto.CartItemDTO;
import java.util.List;

public interface CartService {
    // 添加商品到购物车
    void addToCart(Long userId, CartItemDTO item);

    // 更新购物车商品数量
    void updateQuantity(Long userId, Long productId, Integer quantity);

    // 从购物车删除商品
    void removeFromCart(Long userId, Long productId);

    // 获取用户的购物车
    List<CartItemDTO> getUserCart(Long userId);

    // 清空购物车
    void clearCart(Long userId);

    // 检查商品是否在购物车中
    boolean isProductInCart(String userId, Long productId);

    // 添加并检查购物车
    void addToCartWithCheck(String userId, Long productId, Integer quantity);

    void updateCartItemPaid_quantity(String userId, String productId, Integer paidQuantity);
   
}