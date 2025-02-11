package com.wyc21.mapper;

import com.wyc21.entity.Order;
import com.wyc21.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单
     */
    int insert(Order order);

    /**
     * 根据ID查询订单
     */
    Order findById(@Param("orderId") String orderId);

    /**
   

    /**
     * 更新订单状态
     */
    int updateStatus(
            @Param("orderId") String orderId,
            @Param("oldStatus") String oldStatus,
            @Param("newStatus") String newStatus,
            @Param("version") Integer version);

    /**
     * 更新订单信息
     */
    int updateOrder(Order order);

    /**
     * 查询过期订单
     */
    List<Order> findExpiredOrders(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 插入订单项
     */
    int insertOrderItem(OrderItem orderItem);

    /**
     * 查询订单项
     */
    List<OrderItem> findOrderItems(@Param("orderId") String orderId);
}