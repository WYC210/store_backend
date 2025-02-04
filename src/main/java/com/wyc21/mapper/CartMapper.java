package com.wyc21.mapper;

import com.wyc21.dto.CartItemDTO;
import com.wyc21.entity.Cart;
import com.wyc21.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper {
    Cart findByUserId(Long userId);

    void insert(Cart cart);

    CartItem findCartItem(@Param("cartId") Long cartId, @Param("productId") Long productId);

    void insertCartItem(CartItem item);

    void updateCartItem(CartItem item);

    void deleteCartItem(@Param("cartId") Long cartId, @Param("productId") Long productId);

    List<CartItemDTO> findCartItems(Long userId);

    void deleteAllCartItems(Long cartId);
}