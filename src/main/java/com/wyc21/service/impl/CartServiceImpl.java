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
import com.wyc21.util.JsonResult;
import com.wyc21.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wyc21.entity.Order;
import com.wyc21.entity.OrderItem;
import com.wyc21.entity.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Date;
import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.Random;
import com.wyc21.mapper.OrderMapper;
import com.wyc21.service.ex.InsuffientStockException;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
import com.wyc21.service.ex.InsertException;
// 导入 Map 和 HashMap
import java.util.Map;
import java.util.HashMap;
import com.wyc21.util.JsonResult; // 导入 JsonResult 类
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CartServiceImpl implements ICartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SnowflakeIdGenerator idGenerator;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final long ORDER_EXPIRE_MINUTES = 30;

    @Override
    public List<CartItem> getCartItems(Long userId) {
        // 检查用户是否存在
        User user = userMapper.findByUid(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }

        // 获取购物车项
        List<CartItem> items = cartMapper.findCartItems(userId);

        return items != null ? items : new ArrayList<>();
    }

    @Override
    public CartItem getCartItem(Long userId, String cartItemId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            throw new CartNotFoundException("购物车不存在");
        }
        // CartItem item = cartMapper.findCartItemById(String.valueOf(cartItemId));
        // if (item == null || !item.getCartId().equals(cart.getCartId())) {

        CartItem item = cartMapper.findCartItemById(cartItemId);
        if (item == null || !item.getCartId().equals(String.valueOf(cart.getCartId()))) {
            throw new CartNotFoundException("购物车商品不存在");
        }
        return item;
    }

    @Override
    @Transactional
    public CartItem updateQuantity(Long userId, String cartItemId, Integer quantity) {
        CartItem item = getCartItem(userId, cartItemId);

        // 检查库存
        Product product = productMapper.findById(Long.parseLong(item.getProductId()));
        if (product.getStock() < quantity) {
            throw new ProductNotFoundException("商品库存不足");
        }

        item.setQuantity(quantity);
        cartMapper.updateCartItem(item);
        return item;
    }

    @Override
    @Transactional
    public void deleteCartItem(Long userId, String cartItemId) {
        CartItem item = cartMapper.findCartItemById(cartItemId);

        if (item == null) {
            throw new CartNotFoundException("购物车商品不存在");
        }

        // 验证购物车项是否属于当前用户
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null || !item.getCartId().equals(String.valueOf(cart.getCartId()))) {
            throw new CartNotFoundException("无权操作此购物车商品");
        }

        // 验证通过，执行删除
        cartMapper.deleteCartItem(cartItemId);
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

    @Override
    @Transactional
    public CartItem addToCartWithCheck(Long userId, String productId, Integer quantity) {
        // 检查用户是否存在
        User user = userMapper.findByUid(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }

        // 检查商品是否存在
        Product product = productMapper.findById(Long.parseLong(productId));
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
            // 如果购物车不存在，创建新的购物车
            cart = new Cart();
            cart.setCartId(idGenerator.nextId());
            cart.setUserId(userId);
            cart.setCreatedUser("system");
            cartMapper.insert(cart);
        } else {
            // 检查购物车中的商品状态
            List<CartItem> existingItems = cartMapper.findCartItemsWithStatus(userId);
            boolean hasUnpaidItems = existingItems.stream()
                    .anyMatch(item -> {
                        String orderStatus = item.getOrderStatus();
                        return orderStatus == null ||
                                orderStatus.equals("PENDING_PAY") ||
                                orderStatus.equals("CREATED");
                    });

            if (hasUnpaidItems) {
                // 如果购物车中有待支付或创建状态的商品，直接添加商品
                CartItem existingItem = cartMapper.findCartItem(cart.getCartId(), Long.parseLong(productId));
                if (existingItem != null) {
                    // 更新数量
                    existingItem.setQuantity(quantity);
                    cartMapper.updateCartItem(existingItem);
                    return existingItem;
                }
            } else {
                // 如果购物车中的商品全部已支付或被删除，清空购物车
                cartMapper.deleteAllCartItems(cart.getCartId());
            }
        }

        // 添加新商品
        CartItem newItem = new CartItem();
        newItem.setCartItemId(String.valueOf(idGenerator.nextId()));
        newItem.setCartId(String.valueOf(cart.getCartId()));
        newItem.setProductId(String.valueOf(Long.parseLong(productId)));
        newItem.setQuantity(quantity);
        newItem.setPrice(product.getPrice());
        newItem.setProductName(product.getName());
        newItem.setCreatedUser("system");
        cartMapper.insertCartItem(newItem);
        return newItem;
    }
}