package com.wyc21.service;

import com.wyc21.entity.CartItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.wyc21.util.JsonResult;

public interface ICartService {

    /**
     * 添加商品到购物车（带状态检查）
     */
    CartItem addToCartWithCheck(Long userId, String productId, Integer quantity);

    /**
     * 获取用户的购物车商品列表
     */
    List<CartItem> getCartItems(Long userId);

    /**
     * 获取特定的购物车商品
     */
    CartItem getCartItem(Long userId, String cartItemId);

    /**
     * 更新购物车商品数量
     */
    CartItem updateQuantity(Long userId, String cartItemId, Integer quantity);

    /**
     * 删除购物车商品
     */
    void deleteCartItem(Long userId, String cartItemId);

    /**
     * 清空购物车
     */
    void clearCart(Long userId);

    /**
     * 获取购物车总金额
     */
    BigDecimal getCartTotal(Long userId);

}