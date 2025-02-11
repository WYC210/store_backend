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
     * 支付订单
     */
    boolean payOrder(String orderId, String paymentId);

    /**
     * 取消订单
     */
    void cancelOrder(String orderId);

    /**
     * 获取订单信息
     */
    Order getOrder(String orderId);

    /**
     * 检查并处理过期订单
     */
    void checkExpiredOrders();
} 