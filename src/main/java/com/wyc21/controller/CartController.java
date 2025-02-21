package com.wyc21.controller;

import com.wyc21.entity.CartItem;
import com.wyc21.service.ICartService;
import com.wyc21.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import lombok.Data;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ICartService cartService;

    private static final int OK = 200;
    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public JsonResult<CartItem> addToCart(@RequestBody CartItemRequest request,
            HttpServletRequest httpRequest) {
        String userId = httpRequest.getAttribute("uid").toString();
        return new JsonResult<>(OK,
                cartService.addToCartWithCheck(userId, request.getProductId(), request.getQuantity()));
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public JsonResult<List<CartItem>> getCartItems(HttpServletRequest request) {
        String userId = request.getAttribute("uid").toString();
        return new JsonResult<>(OK, cartService.getCartItems(userId));
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/update/{cartItemId}")
    public JsonResult<CartItem> updateQuantity(@PathVariable String cartItemId,
            @RequestParam Integer quantity,
            HttpServletRequest request) {
        String userId = request.getAttribute("uid").toString();
        return new JsonResult<>(OK,
                cartService.updateQuantity(userId, cartItemId, quantity));
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/delete/{cartItemId}")
    public JsonResult<Void> deleteCartItem(@PathVariable String cartItemId,
            HttpServletRequest request) {
        String userId = request.getAttribute("uid").toString();
        cartService.deleteCartItem(userId, cartItemId);
        return new JsonResult<>(OK);
    }

    @Data
    public static class CartItemRequest {
        private String productId;
        private Integer quantity;
    }
}