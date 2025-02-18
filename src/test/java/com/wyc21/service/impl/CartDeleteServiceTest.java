package com.wyc21.service.impl;

import com.wyc21.entity.Cart;
import com.wyc21.entity.CartItem;
import com.wyc21.entity.User;
import com.wyc21.mapper.CartMapper;
import com.wyc21.mapper.UserMapper;
import com.wyc21.service.ex.CartNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

@SpringBootTest
@Transactional
public class CartDeleteServiceTest {

    @Autowired
    private CartServiceImpl cartService;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private UserMapper userMapper;

    private Cart cart;
    private CartItem cartItem;

    private final String TEST_USER_ID = "100";
    private final String TEST_CART_ID = "200";
    private final String TEST_CART_ITEM_ID = "300";

    @BeforeEach
    public void setUp() {
        // 创建测试用的用户
        User user = new User();
        user.setUid(TEST_USER_ID);
        user.setUsername("testUser");
        user.setPassword("testPassword");
        user.setPower("user");
        userMapper.insert(user);

        // 创建测试用的购物车
        cart = new Cart();
        cart.setCartId(TEST_CART_ID);
        cart.setUserId(TEST_USER_ID);
        cart.setCreatedUser("test");
        cartMapper.insertCart(cart);

        // 创建测试用的购物车项
        cartItem = new CartItem();
        cartItem.setCartItemId(TEST_CART_ITEM_ID);
        cartItem.setCartId(TEST_CART_ID);
        cartItem.setProductId("1");
        cartItem.setQuantity(1);
        cartItem.setPrice(new BigDecimal("99.00"));
        cartItem.setProductName("Test Product");
        cartItem.setCreatedUser("test");
        cartMapper.insertCartItem(cartItem);
    }

    @Test
    @DisplayName("测试成功删除购物车商品")
    public void testDeleteCartItemSuccess() {
        assertDoesNotThrow(() -> cartService.deleteCartItem(TEST_USER_ID, TEST_CART_ITEM_ID));
        CartItem deletedItem = cartMapper.findCartItemById(TEST_CART_ITEM_ID);
        assertNull(deletedItem, "购物车商品应该已被删除");
    }

    @Test
    @DisplayName("测试删除不存在的购物车商品")
    public void testDeleteNonExistentCartItem() {
        String nonExistentItemId = "999";

        CartNotFoundException exception = assertThrows(
                CartNotFoundException.class,
                () -> cartService.deleteCartItem(TEST_USER_ID, nonExistentItemId));

        assertEquals("购物车商品不存在", exception.getMessage());
    }

    @Test
    @DisplayName("测试删除其他用户的购物车商品")
    public void testDeleteOtherUsersCartItem() {
        String otherUserId = "999";

        CartNotFoundException exception = assertThrows(
                CartNotFoundException.class,
                () -> cartService.deleteCartItem(otherUserId, TEST_CART_ITEM_ID));

        assertEquals("购物车商品不存在", exception.getMessage());
    }

    @Test
    @DisplayName("测试清空购物车")
    public void testClearCart() {
        cartService.clearCart(TEST_USER_ID);
        assertTrue(cartMapper.findCartItems(TEST_USER_ID).isEmpty(), "购物车应该为空");
    }

    @Test
    @DisplayName("测试SQL注入防护")
    public void testSqlInjectionProtection() {
        String maliciousId = "1; DROP TABLE wz_cart_items;";

        assertThrows(
                Exception.class,
                () -> cartService.deleteCartItem(TEST_USER_ID, (maliciousId)));
    }
}