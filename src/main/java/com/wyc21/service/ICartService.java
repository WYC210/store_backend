package com.wyc21.service;

import com.wyc21.entity.CartItem;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

public interface ICartService {
    /**
     * 添加单个商品到购物车
     */
    CartItem addToCartWithCheck(String userId, String productId, Integer quantity);

    /**
     * 批量添加商品到购物车
     */
    List<CartItem> batchAddToCart(String userId, List<CartItemRequest> items);

    /**
     * 获取用户的购物车商品列表
     */
    List<CartItem> getCartItems(String userId);

    /**
     * 获取特定的购物车商品
     */
    CartItem getCartItem(String userId, String cartItemId);

    /**
     * 更新购物车商品数量
     */
    CartItem updateQuantity(String userId, String cartItemId, Integer quantity);

    /**
     * 删除购物车商品
     */
    void deleteCartItem(String userId, String cartItemId);

    /**
     * 清空购物车
     */
    void clearCart(String userId);

    /**
     * 获取购物车总金额
     */
    BigDecimal getCartTotal(String userId);

    /**
     * 根据ID列表获取购物车商品
     */
    List<CartItem> getCartItemsByIds(String userId, List<String> cartItemIds);

    /**
     * 更新购物车项的已支付数量
     */
    void updateCartItemPaid_quantity(String userId, String productId, Integer paidQuantity);

    /**
     * 获取购物车项的可用数量
     */
    List<CartItem> getCartItemAvailableQuantity(String cartId);

    @Data
    public static class CartItemRequest {
        private String productId;
        private Integer quantity;
    }
}