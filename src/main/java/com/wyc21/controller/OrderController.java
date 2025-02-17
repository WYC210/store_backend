package com.wyc21.controller;

import com.wyc21.entity.Order;
import com.wyc21.entity.CartItem;
import com.wyc21.service.IOrderService;
import com.wyc21.service.ICartService;
import com.wyc21.util.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

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
        // 获取前端传来的选中商品列表
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> selectedItems = (List<Map<String, Object>>) request.get("items");
        
        if (selectedItems == null || selectedItems.isEmpty()) {
            return new JsonResult<>(400, null, "请选择要购买的商品");
        }

        // 将选中的商品转换为CartItem对象
        List<CartItem> itemsToPurchase = selectedItems.stream()
            .map(item -> {
                CartItem cartItem = new CartItem();
                cartItem.setProductId(String.valueOf(item.get("productId")));
                cartItem.setQuantity(Integer.parseInt(item.get("quantity").toString()));
                cartItem.setPrice(new BigDecimal(item.get("price").toString()));
                cartItem.setProductName((String) item.get("productName"));
                return cartItem;
            })
            .collect(Collectors.toList());

        Order createdOrder = orderService.createOrder(userId, itemsToPurchase);
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

    @PostMapping("/updateStatus")
    public JsonResult<Void> updateOrderStatus(@RequestBody Map<String, Object> params) {
        String orderId = (String) params.get("orderId");
        String status = (String) params.get("status");
        String modifiedUser = (String) params.get("modifiedUser");

        // 调用服务层方法更新订单状态
        orderService.updateOrderStatus(orderId, status, modifiedUser);
        return new JsonResult<>(200, null, "订单状态更新成功");
    }

    @PostMapping("/purchase")
    public JsonResult<Map<String, Object>> purchaseProduct(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("uid");
        
        log.info("接收到购买请求: {}", request);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> selectedItems = (List<Map<String, Object>>) request.get("items");
        
        if (selectedItems == null || selectedItems.isEmpty()) {
            log.warn("没有选择商品: request = {}", request);
            return new JsonResult<>(400, null, "请选择要购买的商品");
        }

        try {
            List<CartItem> itemsToPurchase = selectedItems.stream()
                .map(item -> {
                    CartItem cartItem = new CartItem();
                    cartItem.setProductId(String.valueOf(item.get("productId")));
                    cartItem.setQuantity(Integer.parseInt(item.get("quantity").toString()));
                    cartItem.setPrice(new BigDecimal(String.valueOf(item.get("price"))));
                    cartItem.setProductName((String) item.get("productName"));
                    return cartItem;
                })
                .collect(Collectors.toList());

            log.info("转换后的购物项: {}", itemsToPurchase);
            return orderService.purchaseProduct(userId, itemsToPurchase);
        } catch (Exception e) {
            log.error("购买商品时发生错误: ", e);
            return new JsonResult<>(500, null, "购买失败：" + e.getMessage());
        }
    }
}