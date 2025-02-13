package com.wyc21.mapper;

import com.wyc21.entity.Order;
import com.wyc21.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;
import com.wyc21.entity.OrderStatus;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单
     */
    void insert(Order order);

    /**
     * 根据ID查询订单
     */
    Order findById(@Param("orderId") String orderId);

    /**
     * 根据用户ID查询订单
     */
    List<Order> findByUserId(@Param("userId") Long userId);

    /**
     * 更新订单状态
     */
    int updateOrder(Order order);

    /**
     * 查询过期订单
     */
    List<Order> findExpiredOrders(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 插入订单项
     */
    void insertOrderItem(OrderItem orderItem);

    /**
     * 查询订单项
     */
    List<OrderItem> findOrderItems(@Param("orderId") String orderId);

    int updateOrderStatus(@Param("orderId") String orderId,
                         @Param("status") OrderStatus status,
                         @Param("oldStatus") OrderStatus oldStatus,
                         @Param("version") Integer version);
}