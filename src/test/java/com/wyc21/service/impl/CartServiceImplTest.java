package com.wyc21.service.impl;

import com.wyc21.entity.Cart;
import com.wyc21.entity.CartItem;
import com.wyc21.entity.Product;
import com.wyc21.entity.User;
import com.wyc21.mapper.CartMapper;
import com.wyc21.mapper.ProductMapper;
import com.wyc21.mapper.UserMapper;
import com.wyc21.service.ex.CartNotFoundException;
import com.wyc21.service.ex.ProductNotFoundException;
import com.wyc21.service.ex.UserNotFoundException;
import com.wyc21.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CartServiceImplTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SnowflakeIdGenerator idGenerator;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUid(1L);
        user.setUsername("testUser");

        product = new Product();
        product.setProductId(1L);
        product.setPrice(new BigDecimal("99.00"));
        product.setStock(10);
        product.setName("Test Product");

        cart = new Cart();
        cart.setCartId(1L);
        cart.setUserId(user.getUid());
    }

    @Test
    public void testAddToCart() {
        when(userMapper.findByUid(user.getUid())).thenReturn(user);
        when(productMapper.findById(product.getProductId())).thenReturn(product);
        when(cartMapper.findByUserId(user.getUid())).thenReturn(cart);
        when(idGenerator.nextId()).thenReturn(1L);

        CartItem cartItem = cartService.addToCartWithCheck(user.getUid(), String.valueOf(product.getProductId()), 1);

        assertNotNull(cartItem);
        assertEquals(product.getProductId(), cartItem.getProductId());
        verify(cartMapper, times(1)).insertCartItem(any(CartItem.class));
    }

    // @Test
    // public void testDeleteCartItem() {
    //     when(cartMapper.findByUserId(user.getUid())).thenReturn(cart);
    //     when(cartMapper.findCartItemById(user.getUid(), 1L)).thenReturn(new CartItem());

    //     assertDoesNotThrow(() -> cartService.deleteCartItem(user.getUid(), 1L));
    //     verify(cartMapper, times(1)).deleteCartItem(1L);
    // }

    // @Test
    // public void testDeleteCartItemNotFound() {
    //     when(cartMapper.findByUserId(user.getUid())).thenReturn(cart);
    //     when(cartMapper.findCartItemById(user.getUid(), 1L)).thenReturn(null);

    //     assertThrows(CartNotFoundException.class, () -> cartService.deleteCartItem(user.getUid(), 1L));
    // }

    @Test
    public void testGetCartItems() {
        when(userMapper.findByUid(user.getUid())).thenReturn(user);
        when(cartMapper.findCartItems(user.getUid())).thenReturn(List.of(new CartItem()));

        List<CartItem> items = cartService.getCartItems(user.getUid());

        assertNotNull(items);
        assertFalse(items.isEmpty());
    }

    @Test
    public void testGetCartItemsUserNotFound() {
        when(userMapper.findByUid(user.getUid())).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> cartService.getCartItems(user.getUid()));
    }
} 