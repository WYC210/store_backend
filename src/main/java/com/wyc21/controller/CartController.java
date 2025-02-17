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
import com.wyc21.service.ex.CartNotFoundException;
import com.wyc21.service.ex.ProductNotFoundException;
import com.wyc21.service.ex.UserNotFoundException;
import com.wyc21.service.ex.InsuffientStockException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/cart")
public class CartController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private ICartService cartService;

    @GetMapping
    public JsonResult<List<CartItem>> getCartItems(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("uid");
        List<CartItem> items = cartService.getCartItems(userId);

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
        CartItem updated = cartService.updateQuantity(userId, String.valueOf(cartItemId), quantity);
        return new JsonResult<>(OK, updated, "更新成功");
    }

    @DeleteMapping("/{cartItemId}")
    public JsonResult<Void> deleteCartItem(
            @PathVariable String cartItemId,
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

    @PostMapping("/add")
    public JsonResult<CartItem> addToCart(@RequestBody CartItem cartItem, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("uid");
        CartItem added = cartService.addToCartWithCheck(userId, cartItem.getProductId(), cartItem.getQuantity());
        return new JsonResult<>(OK, added, "添加成功");
    }
}