package com.wyc21.service;

import com.wyc21.dto.CartItemDTO;
import com.wyc21.entity.Product;
import com.wyc21.entity.User;
import com.wyc21.service.ex.BusinessException;
import com.wyc21.mapper.ProductMapper;
import com.wyc21.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CartServiceTests {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    private Long userId;
    private Long productId;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        User user = new User();
        user.setUid(1001L);
        user.setUsername("test_user");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        user.setPower("user");
        userMapper.insert(user);
        userId = user.getUid();

        // 创建测试商品
        Product product = new Product();
        product.setProductId(1001L);
        product.setName("测试商品");
        product.setPrice(new BigDecimal("99.99"));
        product.setStock(100);
        productMapper.insert(product);
        productId = product.getProductId();
    }

    @Test
    void testAddToCart() {
        CartItemDTO item = new CartItemDTO();
        item.setProductId(productId);
        item.setQuantity(2);

        // 测试添加商品到购物车
        assertDoesNotThrow(() -> cartService.addToCart(userId, item));

        // 验证商品是否在购物车中
        assertTrue(cartService.isProductInCart(userId, productId));

        // 验证购物车商品列表
        List<CartItemDTO> cartItems = cartService.getUserCart(userId);
        assertEquals(1, cartItems.size());
        assertEquals(productId, cartItems.get(0).getProductId());
        assertEquals(2, cartItems.get(0).getQuantity());
    }

    @Test
    void testUpdateQuantity() {
        // 先添加商品到购物车
        CartItemDTO item = new CartItemDTO();
        item.setProductId(productId);
        item.setQuantity(1);
        cartService.addToCart(userId, item);

        // 测试更新数量
        assertDoesNotThrow(() -> cartService.updateQuantity(userId, productId, 3));

        // 验证数量是否更新
        List<CartItemDTO> cartItems = cartService.getUserCart(userId);
        assertEquals(3, cartItems.get(0).getQuantity());
    }

    @Test
    void testRemoveFromCart() {
        // 先添加商品到购物车
        CartItemDTO item = new CartItemDTO();
        item.setProductId(productId);
        item.setQuantity(1);
        cartService.addToCart(userId, item);

        // 测试删除商品
        assertDoesNotThrow(() -> cartService.removeFromCart(userId, productId));

        // 验证商品是否已删除
        assertFalse(cartService.isProductInCart(userId, productId));
        assertTrue(cartService.getUserCart(userId).isEmpty());
    }

    @Test
    void testClearCart() {
        // 添加多个商品到购物车
        CartItemDTO item1 = new CartItemDTO();
        item1.setProductId(productId);
        item1.setQuantity(1);
        cartService.addToCart(userId, item1);

        // 测试清空购物车
        assertDoesNotThrow(() -> cartService.clearCart(userId));

        // 验证购物车是否为空
        assertTrue(cartService.getUserCart(userId).isEmpty());
    }

    @Test
    void testAddToCartWithInsufficientStock() {
        CartItemDTO item = new CartItemDTO();
        item.setProductId(productId);
        item.setQuantity(101); // 库存只有100

        // 测试添加超出库存的商品
        assertThrows(BusinessException.class, () -> cartService.addToCart(userId, item));
    }
}