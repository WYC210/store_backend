package com.wyc21.service;

import com.wyc21.entity.Cart;
import com.wyc21.entity.CartItem;
import com.wyc21.service.ex.CartNotFoundException;
import com.wyc21.service.ex.ProductNotFoundException;
import com.wyc21.service.ex.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
public class CartServiceTests {

    @Autowired
    private ICartService cartService;

    @Autowired
    private DatabaseInitService databaseInitService;

    @BeforeEach
    void setUp() {
        databaseInitService.initializeDatabase();
    }

    @Test
    public void testAddToCart() {
        log.info("开始测试添加商品到购物车");

        String userId = "2";
        String productId = "1";
        Integer quantity = 1;

        CartItem cartItem = cartService.addToCartWithCheck(userId, productId, quantity);

        assertNotNull(cartItem, "购物车项不应为null");
        assertEquals(productId, cartItem.getProductId(), "商品ID应匹配");
        assertEquals(quantity, cartItem.getQuantity(), "商品数量应匹配");
        assertEquals(new BigDecimal("6999.00"), cartItem.getPrice(), "商品价格应匹配");
        assertEquals("iPhone 14", cartItem.getProductName(), "商品名称应匹配");

        log.info("添加购物车测试完成");
    }

    @Test
    public void testAddToCartWithInvalidUser() {
        log.info("开始测试无效用户添加购物车");

        String invalidUserId = "99999";
        String productId = "1";
        Integer quantity = 1;

        assertThrows(UserNotFoundException.class, () -> {
            cartService.addToCartWithCheck(invalidUserId, productId, quantity);
        });

        log.info("无效用户测试完成");
    }

    @Test
    public void testAddToCartWithInvalidProduct() {
        log.info("开始测试添加无效商品");

        String userId = "2";
        String invalidProductId = "99999";
        Integer quantity = 1;

        assertThrows(ProductNotFoundException.class, () -> {
            cartService.addToCartWithCheck(userId, invalidProductId, quantity);
        });

        log.info("无效商品测试完成");
    }

    @Test
    public void testGetCartItems() {
        log.info("开始测试获取购物车商品列表");

        String userId = "2";
        List<CartItem> items = cartService.getCartItems(userId);

        assertNotNull(items, "购物车列表不应为null");
        assertFalse(items.isEmpty(), "购物车不应为空");

        items.forEach(item -> {
            log.info("购物车商品: {}", item.getProductName());
            assertNotNull(item.getCartItemId(), "购物车项ID不应为null");
            assertNotNull(item.getProductId(), "商品ID不应为null");
            assertTrue(item.getQuantity() > 0, "商品数量应大于0");
            assertTrue(item.getPrice().compareTo(BigDecimal.ZERO) > 0, "商品价格应大于0");
        });

        log.info("获取购物车列表测试完成");
    }

    @Test
    public void testUpdateQuantity() {
        log.info("开始测试更新购物车商品数量");

        String userId = "2";
        String cartItemId = "1";
        Integer newQuantity = 2;

        CartItem updatedItem = cartService.updateQuantity(userId, cartItemId, newQuantity);

        assertNotNull(updatedItem, "更新后的购物车项不应为null");
        assertEquals(newQuantity, updatedItem.getQuantity(), "商品数量应该更新成功");

        log.info("更新商品数量测试完成");
    }

    @Test
    public void testDeleteCartItem() {
        log.info("开始测试删除购物车商品");

        String userId = "2";
        String cartItemId = "1";

        cartService.deleteCartItem(userId, cartItemId);

        // 验证删除后无法找到该商品
        assertThrows(CartNotFoundException.class, () -> {
            cartService.getCartItem(userId, cartItemId);
        });

        log.info("删除购物车商品测试完成");
    }

    @Test
    public void testClearCart() {
        log.info("开始测试清空购物车");

        String userId = "2";
        cartService.clearCart(userId);

        List<CartItem> items = cartService.getCartItems(userId);
        assertTrue(items.isEmpty(), "购物车应该为空");

        log.info("清空购物车测试完成");
    }

    @Test
    public void testGetCartTotal() {
        log.info("开始测试获取购物车总金额");

        String userId = "2";
        BigDecimal total = cartService.getCartTotal(userId);

        assertNotNull(total, "总金额不应为null");
        assertTrue(total.compareTo(BigDecimal.ZERO) >= 0, "总金额应该大于等于0");

        log.info("购物车总金额: {}", total);
        log.info("获取购物车总金额测试完成");
    }
}
