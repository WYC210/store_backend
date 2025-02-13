package com.wyc21.service;

import com.wyc21.entity.Order;
import com.wyc21.entity.CartItem;
import java.util.List;

public interface IOrderService {
    /**
     * 创建订单
     */
    Order createOrder(Long userId, List<CartItem> items);

    /**
     * 新增方法：直接购买
     */
    Order createOrderDirect(Long userId, Long productId, Integer quantity);

    /**
     * 获取用户的所有订单
     */
    List<Order> getOrdersByUserId(Long userId);

    /**
     * 获取订单详情
     */
    Order getOrder(String orderId);

    /**
     * 支付订单
     */
    boolean payOrder(String orderId, String paymentId);

    /**
     * 取消订单
     */
    void cancelOrder(String orderId);

    /**
     * 检查并处理过期订单
     */
    void checkExpiredOrders();
}