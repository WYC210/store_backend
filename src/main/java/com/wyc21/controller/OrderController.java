package com.wyc21.controller;

import com.wyc21.entity.Order;
import com.wyc21.entity.CartItem;
import com.wyc21.service.IOrderService;
import com.wyc21.service.ICartService;
import com.wyc21.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController extends BaseController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private ICartService cartService;

    @PostMapping("/create")
    public JsonResult<Order> createOrder(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("uid");
        // 获取购物车中的商品
        List<CartItem> cartItems = cartService.getCartItems(userId);
        if (cartItems.isEmpty()) {
            return new JsonResult<>(400, null, "购物车为空");
        }

        Order createdOrder = orderService.createOrder(userId, cartItems);
        return new JsonResult<>(OK, createdOrder, "订单创建成功");
    }

    @PostMapping("/create/direct")
    public JsonResult<Order> createOrderDirect(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("uid");
        Long productId = Long.parseLong(request.get("productId").toString());
        Integer quantity = Integer.parseInt(request.get("quantity").toString());

        Order createdOrder = orderService.createOrderDirect(userId, productId, quantity);
        return new JsonResult<>(OK, createdOrder, "订单创建成功");
    }

    @GetMapping
    public JsonResult<List<Order>> getOrders(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("uid");
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return new JsonResult<>(OK, orders);
    }

    @GetMapping("/{orderId}")
    public JsonResult<Order> getOrder(@PathVariable String orderId) {
        Order order = orderService.getOrder(orderId);
        return new JsonResult<>(OK, order);
    }

    @PostMapping("/{orderId}/pay")
    public JsonResult<Boolean> payOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, String> paymentInfo) {
        String paymentId = paymentInfo.get("paymentId");
        boolean success = orderService.payOrder(orderId, paymentId);
        return new JsonResult<>(OK, success, success ? "支付成功" : "支付失败");
    }

    @PostMapping("/{orderId}/cancel")
    public JsonResult<Void> cancelOrder(@PathVariable String orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("uid");
        orderService.cancelOrder(orderId, userId);
        return new JsonResult<>(OK, null, "订单取消成功");
    }
}