package com.wyc21.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wyc21.entity.CartItem;
import com.wyc21.service.ICartService;
import com.wyc21.service.TokenService;
import com.wyc21.util.JsonResult;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CartControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ICartService cartService;

    @MockBean
    private TokenService tokenService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testPurchaseWithValidToken() throws Exception {
        // 准备测试数据
        Map<String, Object> purchaseRequest = new HashMap<>();
        purchaseRequest.put("productId", "3");
        purchaseRequest.put("quantity", 1);
        purchaseRequest.put("price", "299.00");
        purchaseRequest.put("productName", "男士休闲夹克");

        // 模拟token验证成功
        String validToken = "Bearer valid_token";
        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getSubject()).thenReturn("testUser");
        when(tokenService.validateToken(any(String.class))).thenReturn(mockClaims);

        // 模拟购买服务
        CartItem mockCartItem = new CartItem();
        mockCartItem.setProductId("3");
        mockCartItem.setQuantity(1);
        mockCartItem.setPrice(new BigDecimal("299.00"));
        when(cartService.addToCartWithCheck(anyString(), anyString(), any())).thenReturn(mockCartItem);

        // 执行测试
        mockMvc.perform(post("/cart/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", validToken)
                .content(objectMapper.writeValueAsString(purchaseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(200))
                .andExpect(jsonPath("$.message").value("添加成功"));
    }

    @Test
    void testPurchaseWithInvalidToken() throws Exception {
        // 准备测试数据
        Map<String, Object> purchaseRequest = new HashMap<>();
        purchaseRequest.put("productId", "3");
        purchaseRequest.put("quantity", 1);
        purchaseRequest.put("price", "299.00");
        purchaseRequest.put("productName", "男士休闲夹克");

        // 模拟token验证失败
        String invalidToken = "Bearer invalid_token";
        when(tokenService.validateToken(any(String.class))).thenReturn(null);

        // 执行测试
        mockMvc.perform(post("/cart/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", invalidToken)
                .content(objectMapper.writeValueAsString(purchaseRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testPurchaseWithMissingToken() throws Exception {
        // 准备测试数据
        Map<String, Object> purchaseRequest = new HashMap<>();
        purchaseRequest.put("productId", "3");
        purchaseRequest.put("quantity", 1);

        // 执行测试
        mockMvc.perform(post("/cart/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(purchaseRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testPurchaseWithInvalidRequestBody() throws Exception {
        // 准备无效的测试数据（缺少必要字段）
        Map<String, Object> invalidRequest = new HashMap<>();
        invalidRequest.put("productId", "3");
        // 缺少 quantity 字段

        // 模拟token验证成功
        String validToken = "Bearer valid_token";
        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getSubject()).thenReturn("testUser");
        when(tokenService.validateToken(any(String.class))).thenReturn(mockClaims);

        // 执行测试
        mockMvc.perform(post("/cart/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", validToken)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}