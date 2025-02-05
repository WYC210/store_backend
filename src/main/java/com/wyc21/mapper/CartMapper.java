package com.wyc21.mapper;

import com.wyc21.entity.Cart;
import com.wyc21.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper {
    /**
     * 根据用户ID查找购物车
     */
    Cart findByUserId(Long userId);

    /**
     * 插入购物车
     */
    void insert(Cart cart);

    /**
     * 根据购物车ID和商品ID查找购物车项
     */
    CartItem findCartItem(@Param("cartId") Long cartId, @Param("productId") Long productId);

    /**
     * 根据购物车项ID查找购物车项
     */
    CartItem findCartItemById(Long cartItemId);

    /**
     * 插入购物车项
     */
    void insertCartItem(CartItem cartItem);

    /**
     * 更新购物车项
     */
    void updateCartItem(CartItem cartItem);

    /**
     * 删除购物车项
     */
    void deleteCartItem(@Param("cartId") Long cartId, @Param("productId") Long productId);

    /**
     * 删除购物车所有商品
     */
    void deleteAllCartItems(Long cartId);

    /**
     * 获取用户购物车所有商品
     */
    List<CartItem> findCartItems(Long userId);
}