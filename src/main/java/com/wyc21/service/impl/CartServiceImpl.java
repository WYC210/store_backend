package com.wyc21.service.impl;

import com.wyc21.entity.Cart;
import com.wyc21.entity.CartItem;
import com.wyc21.entity.Product;
import com.wyc21.entity.User;
import com.wyc21.service.ICartService;
import com.wyc21.service.ex.CartNotFoundException;
import com.wyc21.service.ex.ProductNotFoundException;
import com.wyc21.service.ex.UserNotFoundException;
import com.wyc21.mapper.CartMapper;
import com.wyc21.mapper.ProductMapper;
import com.wyc21.mapper.UserMapper;
import com.wyc21.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SnowflakeIdGenerator idGenerator;

    @Override
    @Transactional
    public CartItem addToCart(Long userId, Long productId, Integer quantity) {
        // 检查用户是否存在
        User user = userMapper.findByUid(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }

        // 检查商品是否存在
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new ProductNotFoundException("商品不存在");
        }

        // 检查库存
        if (product.getStock() < quantity) {
            throw new ProductNotFoundException("商品库存不足");
        }

        // 检查购物车是否存在
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setCartId(idGenerator.nextId());
            cart.setUserId(userId);
            cart.setCreatedUser("system");
            cartMapper.insert(cart);
        }

        // 检查商品是否已在购物车中
        CartItem existingItem = cartMapper.findCartItem(cart.getCartId(), productId);
        if (existingItem != null) {
            // 直接更新为新的数量，而不是相加
            existingItem.setQuantity(quantity);
            cartMapper.updateCartItem(existingItem);
            return existingItem;
        } else {
            // 添加新商品
            CartItem newItem = new CartItem();
            newItem.setCartItemId(idGenerator.nextId());
            newItem.setCartId(cart.getCartId());
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getPrice());
            newItem.setProductName(product.getName());
            newItem.setCreatedUser("system");
            cartMapper.insertCartItem(newItem);
            return newItem;
        }
    }

    @Override
    public List<CartItem> getCartItems(Long userId) {
        return cartMapper.findCartItems(userId);
    }

    @Override
    public CartItem getCartItem(Long userId, Long cartItemId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            throw new CartNotFoundException("购物车不存在");
        }
        CartItem item = cartMapper.findCartItemById(cartItemId);
        if (item == null || !item.getCartId().equals(cart.getCartId())) {
            throw new CartNotFoundException("购物车商品不存在");
        }
        return item;
    }

    @Override
    @Transactional
    public CartItem updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem item = getCartItem(userId, cartItemId);

        // 检查库存
        Product product = productMapper.findById(item.getProductId());
        if (product.getStock() < quantity) {
            throw new ProductNotFoundException("商品库存不足");
        }

        item.setQuantity(quantity);
        cartMapper.updateCartItem(item);
        return item;
    }

    @Override
    @Transactional
    public void deleteCartItem(Long userId, Long cartItemId) {
        CartItem item = getCartItem(userId, cartItemId);
        cartMapper.deleteCartItem(item.getCartId(), item.getProductId());
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
    public BigDecimal getCartTotal(Long userId) {
        List<CartItem> items = getCartItems(userId);
        return items.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}