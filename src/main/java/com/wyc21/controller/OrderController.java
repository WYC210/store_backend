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
import lombok.extern.slf4j.Slf4j;
import java.util.Collections;
import lombok.Data;
import java.util.ArrayList;

@Slf4j
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
        String userId = httpRequest.getAttribute("uid").toString();
        log.info("用户ID: {}, 创建订单请求: {}", userId, request);
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
        log.info("订单创建成功: {}", createdOrder);
        return new JsonResult<>(OK, createdOrder, "订单创建成功");
    }

    @PostMapping("/create/direct")
    public JsonResult<Order> createOrderDirect(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        log.info("收到直接购买请求223333: {}", request);
        try {
            String userId = httpRequest.getAttribute("uid").toString();

            // 从请求中获取商品信息
            @SuppressWarnings("unchecked")
            Map<String, Object> item = (Map<String, Object>) request.get("items");

            if (item == null) {
                log.error("商品信息不能为空");
                return new JsonResult<>(400, null, "商品信息不能为空");
            }

            String productId = item.get("productId").toString();
            Integer quantity = Integer.parseInt(item.get("quantity").toString());

            log.info("用户ID: {}, 商品ID: {}, 数量: {}", userId, productId, quantity);

            // 创建订单
            Order createdOrder = orderService.createOrderDirect(userId, productId, quantity);

            // 返回创建的订单信息
            return new JsonResult<>(OK, createdOrder, "订单创建成功");
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return new JsonResult<>(500, null, "创建订单失败：" + e.getMessage());
        }
    }

    @GetMapping
    public JsonResult<List<Order>> getOrders(HttpServletRequest request) {
        String userId = request.getAttribute("uid").toString();
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
            @RequestBody Map<String, String> paymentInfo,
            HttpServletRequest request) {
        try {
            String userId = request.getAttribute("uid").toString();
            if (userId == null) {
                return new JsonResult<>(403, false, "未授权的访问");
            }

            // 验证订单所属权
            Order order = orderService.getOrder(orderId);
            if (order == null || !order.getUserId().equals(userId)) {
                return new JsonResult<>(403, false, "无权访问此订单");
            }

            String paymentId = paymentInfo.get("paymentId");
            boolean success = orderService.payOrder(orderId, paymentId);
            return new JsonResult<>(OK, success, success ? "支付成功" : "支付失败");
        } catch (Exception e) {
            log.error("支付订单时发生错误: ", e);
            return new JsonResult<>(500, false, "支付失败：" + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/cancel")
    public JsonResult<Void> cancelOrder(@PathVariable String orderId, HttpServletRequest request) {
        String userId = request.getAttribute("uid").toString();
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
    public JsonResult<Map<String, Object>> purchaseProduct(@RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        String userId = httpRequest.getAttribute("uid").toString();

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

    /**
     * 购物车中选中的多个或单个商品结算
     */
    @PostMapping("/purchase/cart")
    public JsonResult<Order> purchaseFromCart(@RequestBody CartPurchaseRequest request,
            HttpServletRequest httpRequest) {
        String userId = httpRequest.getAttribute("uid").toString();
        log.info("===== 购物车结算请求开始 =====");
        log.info("用户ID: {}", userId);
        log.info("购物车项ID2233: {}", request.getCartItemIds());

        try {
            Order order = orderService.createOrderFromCart(userId, request.getCartItemIds());
            return new JsonResult<>(OK, order, "订单创建成功");
        } catch (Exception e) {
            log.error("创建订单失败: ", e);
            return new JsonResult<>(500, null, "创建订单失败：" + e.getMessage());
        }
    }

    @Data
    public static class CartPurchaseRequest {
        private List<String> cartItemIds;
    }
}