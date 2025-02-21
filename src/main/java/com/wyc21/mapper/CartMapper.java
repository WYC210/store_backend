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
    Cart findByUserId(String userId);

    /**
     * 插入购物车
     */
    void insertCart(Cart cart);

    /**
     * 根据购物车ID和商品ID查找购物车项
     */
    CartItem findCartItem(@Param("userId") String userId, @Param("productId") String productId);

    /**
     * 根据购物车项ID查找购物车项
     */
    CartItem findCartItemById(String cartItemId);

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
    void deleteCartItem(@Param("userId") String userId, @Param("cartItemId") String cartItemId);

    /**
     * 删除购物车所有商品
     */
    void deleteAllCartItems(String userId);

    /**
     * 获取用户购物车所有商品
     */
    List<CartItem> findCartItems(String userId);

    /**
     * 查询购物车项及其订单状态
     */
    List<CartItem> findCartItemsWithStatus(String userId);

    List<CartItem> findByIds(@Param("userId") String userId, @Param("cartItemIds") List<String> cartItemIds);

    CartItem findCartItemByProductId(String cartId, String productId);

    /**
     * 批量添加购物车项
     */
    void batchInsertCartItems(List<CartItem> cartItems);

    /**
     * 根据用户ID查找购物车项列表
     */
    List<CartItem> findCartItemsByUserId(String userId);

   
    /**
     * 更新购物车项的已支付数量
     */
    void updateCartItemPaid_quantity(
        @Param("userId") String userId,
        @Param("productId") String productId,
        @Param("paidQuantity") Integer paidQuantity
    );

    /**
     * 查询购物车项的可用数量
     */
    List<CartItem> selectCartItemPaid_quantity(@Param("cartId") String cartId);
}