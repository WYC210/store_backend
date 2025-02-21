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
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
@Slf4j
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
    public List<CartItem> getCartItems(String userId) {
        // 获取用户的购物车商品
        List<CartItem> cartItems = cartMapper.findCartItems(userId);

        // 过滤掉已支付的商品，并更新商品数量
    List<CartItem> availableItems = cartItems.stream()
        .map(item -> {
            // 计算可用数量
            int availableQuantity = item.getQuantity() - item.getPaidQuantity();
            // 如果可用数量大于0，则更新商品数量
            if (availableQuantity > 0) {
                item.setQuantity(availableQuantity); // 更新商品数量
                return item; // 返回更新后的商品
            }
            return null; // 如果可用数量为0，返回null
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    // 如果没有可用商品，返回空列表
    return availableItems;
    }

    @Override
    public CartItem getCartItem(String userId, String cartItemId) {
        Cart userCart = cartMapper.findByUserId(userId);
        if (userCart == null) {
            throw new CartNotFoundException("购物车不存在");
        }

        CartItem item = cartMapper.findCartItemById(cartItemId);
        if (item == null || !item.getCartId().equals(userCart.getCartId())) {
            throw new CartNotFoundException("购物车商品不存在");
        }
        return item;
    }

    @Override
    @Transactional
    public CartItem updateQuantity(String userId, String cartItemId, Integer quantity) {
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
    public void deleteCartItem(String userId, String cartItemId) {
        CartItem item = cartMapper.findCartItemById(cartItemId);
        if (item == null) {
            throw new CartNotFoundException("购物车商品不存在");
        }

        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null || !item.getCartId().equals(cart.getCartId())) {
            throw new CartNotFoundException("无权操作此购物车商品");
        }

        cartMapper.deleteCartItem(userId, cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart != null) {
            cartMapper.deleteAllCartItems(cart.getCartId());
        }
    }

    @Override
    public BigDecimal getCartTotal(String userId) {
        List<CartItem> items = getCartItems(userId);
        return items.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public CartItem addToCartWithCheck(String userId, String productId, Integer quantity) {
        // 检查用户是否存在
        User user = userMapper.findByUid(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }

        // 获取或创建购物车
        Cart userCart = cartMapper.findByUserId(userId);
        if (userCart == null) {
            userCart = new Cart();
            userCart.setCartId(idGenerator.nextId().toString());
            userCart.setUserId(userId);
            userCart.setCreatedUser("system");
            userCart.setCreatedTime(LocalDateTime.now());
            cartMapper.insertCart(userCart);
        }

        // 检查商品是否已在购物车中
        CartItem existingItem = cartMapper.findCartItemByProductId(userCart.getCartId(), productId);
        if (existingItem != null) {
            // 更新数量
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartMapper.updateCartItem(existingItem);
            return existingItem;
        }

        // 检查商品是否存在并验证库存
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new ProductNotFoundException("商品不存在");
        }
        if (product.getStock() < quantity) {
            throw new InsuffientStockException("商品库存不足");
        }

        // 创建新的购物车项
        CartItem cartItem = new CartItem();
        cartItem.setCartItemId(idGenerator.nextId().toString());
        cartItem.setCartId(userCart.getCartId());
        cartItem.setProductId(productId);
        cartItem.setQuantity(quantity);
        cartItem.setPrice(product.getPrice());
        cartItem.setProductName(product.getName());
        cartItem.setCreatedUser("system");
        cartItem.setCreatedTime(LocalDateTime.now());
        cartMapper.insertCartItem(cartItem);

        return cartItem;
    }

    @Override
    @Transactional
    public List<CartItem> batchAddToCart(String userId, List<ICartService.CartItemRequest> items) {
        // 检查用户是否存在
        User user = userMapper.findByUid(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }

        // 获取或创建购物车
        Cart userCart = cartMapper.findByUserId(userId);
        if (userCart == null) {
            userCart = new Cart();
            userCart.setCartId(idGenerator.nextId().toString());
            userCart.setUserId(userId);
            userCart.setCreatedUser("system");
            userCart.setCreatedTime(LocalDateTime.now());
            cartMapper.insertCart(userCart);
        }

        List<CartItem> newCartItems = new ArrayList<>();

        for (ICartService.CartItemRequest itemRequest : items) {
            // 检查商品是否存在并验证库存
            Product product = productMapper.findById(itemRequest.getProductId());
            if (product == null) {
                throw new ProductNotFoundException("商品不存在: " + itemRequest.getProductId());
            }
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new InsuffientStockException("商品库存不足: " + product.getName());
            }

            // 检查商品是否已在购物车中
            CartItem existingItem = cartMapper.findCartItemByProductId(userCart.getCartId(),
                    itemRequest.getProductId());
            if (existingItem != null) {
                // 更新数量
                existingItem.setQuantity(existingItem.getQuantity() + itemRequest.getQuantity());
                cartMapper.updateCartItem(existingItem);
                newCartItems.add(existingItem);
            } else {
                // 创建新的购物车项
                CartItem cartItem = new CartItem();
                cartItem.setCartItemId(idGenerator.nextId().toString());
                cartItem.setCartId(userCart.getCartId());
                cartItem.setProductId(itemRequest.getProductId());
                cartItem.setQuantity(itemRequest.getQuantity());
                cartItem.setPrice(product.getPrice());
                cartItem.setProductName(product.getName());
                cartItem.setCreatedUser("system");
                cartItem.setCreatedTime(LocalDateTime.now());
                newCartItems.add(cartItem);
            }
        }

        // 批量插入新的购物车项
        if (!newCartItems.isEmpty()) {
            cartMapper.batchInsertCartItems(newCartItems);
        }

        return newCartItems;
    }

    private CartItem validateCartItem(String userId, String cartItemId) {
        CartItem item = cartMapper.findCartItemById(cartItemId);
        Cart userCart = cartMapper.findByUserId(userId);
        if (item == null || userCart == null) {
            throw new CartNotFoundException("购物车商品不存在");
        }
        return item;
    }

    private void validateCartItemOwnership(String userId, CartItem item) {
        List<CartItem> userCartItems = cartMapper.findCartItemsByUserId(userId);
        if (userCartItems.isEmpty()) {
            throw new CartNotFoundException("无权操作此购物车商品");
        }
    }

    private boolean checkExistingItems(List<CartItem> existingItems) {
        return existingItems.stream()
                .anyMatch(item -> {
                    String orderStatus = item.getOrderStatus();
                    return orderStatus == null ||
                            orderStatus.equals("PENDING_PAY") ||
                            orderStatus.equals("CREATED");
                });
    }

    private CartItem createNewCartItem(String userId, Integer quantity, Product product) {
        CartItem newItem = new CartItem();
        newItem.setCartItemId(String.valueOf(idGenerator.nextId()));
        newItem.setCartId(String.valueOf(userId));
        newItem.setProductId(String.valueOf(product.getProductId()));
        newItem.setQuantity(quantity);
        newItem.setPrice(product.getPrice());
        newItem.setProductName(product.getName());
        newItem.setImageUrl(product.getImageUrl());
        newItem.setOrderStatus("CREATED");
        return newItem;
    }

    @Override
    public List<CartItem> getCartItemsByIds(String userId, List<String> cartItemIds) {
        // 验证用户
        User user = userMapper.findByUid(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }
        // 获取该用户的所有购物车项
        List<CartItem> allCartItems = cartMapper.findCartItemsByUserId(userId);

        // 筛选出需要的购物车项
        List<CartItem> filteredCartItems = allCartItems.stream()
                .filter(cartItem -> cartItemIds.contains(cartItem.getCartItemId()))
                .collect(Collectors.toList());
        
        return filteredCartItems;
    }

    private void validateProduct(String productId, Integer quantity) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new ProductNotFoundException("商品不存在");
        }
        if (product.getStock() < quantity) {
            throw new InsuffientStockException("商品库存不足");
        }
    }

    @Override
    @Transactional
    public void updateCartItemPaid_quantity(String userId, String productId, Integer paidQuantity) {
        if (userId == null || productId == null || paidQuantity == null) {
            log.error("更新购物车支付数量参数错误: userId={}, productId={}, paidQuantity={}",
                    userId, productId, paidQuantity);
            throw new IllegalArgumentException("参数不能为空");
        }

        try {
            cartMapper.updateCartItemPaid_quantity(userId, productId, paidQuantity);
            log.info("更新购物车支付数量成功: userId={}, productId={}, paidQuantity={}",
                    userId, productId, paidQuantity);
        } catch (Exception e) {
            log.error("更新购物车支付数量失败", e);
            throw new RuntimeException("更新购物车支付数量失败: " + e.getMessage());
        }
    }

    @Override
    public List<CartItem> getCartItemAvailableQuantity(String cartId) {
        return cartMapper.selectCartItemPaid_quantity(cartId);
    }

}