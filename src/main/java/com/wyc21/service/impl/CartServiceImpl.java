package com.wyc21.service.impl;

import com.wyc21.dto.CartItemDTO;
import com.wyc21.entity.Cart;
import com.wyc21.entity.CartItem;
import com.wyc21.entity.Product;
import com.wyc21.service.ex.BusinessException;
import com.wyc21.mapper.CartMapper;
import com.wyc21.mapper.ProductMapper;
import com.wyc21.service.CartService;
import com.wyc21.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SnowflakeIdGenerator idGenerator;

    @Override
    @Transactional
    public void addToCart(Long userId, CartItemDTO itemDTO) {
        // 检查商品是否存在
        Product product = productMapper.findById(itemDTO.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 检查库存
        if (product.getStock() < itemDTO.getQuantity()) {
            throw new BusinessException("商品库存不足");
        }

        // 检查购物车是否存在
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setCartId(idGenerator.nextId());
            cart.setUserId(userId);
            cartMapper.insert(cart);
        }

        // 检查商品是否已在购物车中
        CartItem existingItem = cartMapper.findCartItem(cart.getCartId(), itemDTO.getProductId());
        if (existingItem != null) {
            // 更新数量
            existingItem.setQuantity(existingItem.getQuantity() + itemDTO.getQuantity());
            cartMapper.updateCartItem(existingItem);
        } else {
            // 添加新商品
            CartItem newItem = new CartItem();
            newItem.setCartItemId(idGenerator.nextId());
            newItem.setCartId(cart.getCartId());
            newItem.setProductId(itemDTO.getProductId());
            newItem.setQuantity(itemDTO.getQuantity());
            newItem.setPrice(product.getPrice());
            newItem.setProductName(product.getName());
            cartMapper.insertCartItem(newItem);
        }
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            throw new BusinessException("购物车不存在");
        }

        CartItem item = cartMapper.findCartItem(cart.getCartId(), productId);
        if (item == null) {
            throw new BusinessException("商品不在购物车中");
        }

        // 检查库存
        Product product = productMapper.findById(productId);
        if (product.getStock() < quantity) {
            throw new BusinessException("商品库存不足");
        }

        item.setQuantity(quantity);
        cartMapper.updateCartItem(item);
    }

    @Override
    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            return;
        }
        cartMapper.deleteCartItem(cart.getCartId(), productId);
    }

    @Override
    public List<CartItemDTO> getUserCart(Long userId) {
        return cartMapper.findCartItems(userId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart != null) {
            cartMapper.deleteAllCartItems(cart.getCartId());
        }
    }

    @Override
    public boolean isProductInCart(Long userId, Long productId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            return false;
        }
        CartItem item = cartMapper.findCartItem(cart.getCartId(), productId);
        return item != null;
    }
}