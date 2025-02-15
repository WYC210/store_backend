package com.wyc21.controller;

import com.wyc21.entity.CartItem;
import com.wyc21.service.ICartService;
import com.wyc21.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/cart")
public class CartController extends BaseController {

    @Autowired
    private ICartService cartService;

    @PostMapping("/add")
    public JsonResult<CartItem> addToCart(@RequestBody CartItem cartItem, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("uid");
        CartItem added = cartService.addToCart(userId, cartItem.getProductId(), cartItem.getQuantity());
        return new JsonResult<>(OK, added, "添加成功");
    }

    @GetMapping
    public JsonResult<List<CartItem>> getCartItems(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("uid");
        List<CartItem> items = cartService.getCartItems(userId);

        // 即使购物车为空，也返回一个空列表和成功状态
        if (items == null || items.isEmpty()) {
            return new JsonResult<>(OK, new ArrayList<>(), "购物车为空");
        }

        return new JsonResult<>(OK, items);
    }

    @PutMapping("/{cartItemId}")
    public JsonResult<CartItem> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("uid");
        CartItem updated = cartService.updateQuantity(userId, cartItemId, quantity);
        return new JsonResult<>(OK, updated, "更新成功");
    }

    @DeleteMapping("/{cartItemId}")
    public JsonResult<Void> deleteCartItem(
            @PathVariable Long cartItemId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("uid");
        cartService.deleteCartItem(userId, cartItemId);
        return new JsonResult<>(OK, null, "删除成功");
    }

    @DeleteMapping
    public JsonResult<Void> clearCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("uid");
        cartService.clearCart(userId);
        return new JsonResult<>(OK, null, "购物车已清空");
    }

    @GetMapping("/total")
    public JsonResult<BigDecimal> getCartTotal(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("uid");
        BigDecimal total = cartService.getCartTotal(userId);
        return new JsonResult<>(OK, total);
    }

    @PostMapping("/purchase")
    public JsonResult<Map<String, Object>> purchaseProduct(@RequestBody CartItem cartItem, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("uid");
        return cartService.purchaseProduct(userId, cartItem.getProductId(), cartItem.getQuantity());
    }
}