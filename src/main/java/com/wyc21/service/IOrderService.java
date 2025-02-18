package com.wyc21.service;

import com.wyc21.entity.Order;
import java.util.List;
import java.util.Map;
import com.wyc21.entity.CartItem;
import com.wyc21.util.JsonResult;

public interface IOrderService {
    /**
     * 直接购买创建订单
     */
    Order createOrderDirect(String userId, String productId, Integer quantity);

    /**
     * 从购物车创建订单
     */
    Order createOrderFromCart(String userId, List<String> cartItemIds);
    

    /**
     * 获取用户的所有订单
     */
    List<Order> getOrdersByUserId(String userId);

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
    void cancelOrder(String orderId, String userId);

    /**
     * 检查并处理过期订单
     */
    void checkExpiredOrders();

    /**
     * 更新订单状态
     */
    void updateOrderStatus(String orderId, String status, String modifiedUser);

    /**
     * 创建订单
     */
    Order createOrder(String userId, List<CartItem> items);

    /**
     * 购买商品
     */
    JsonResult<Map<String, Object>> purchaseProduct(String userId, List<CartItem> items);

  
}