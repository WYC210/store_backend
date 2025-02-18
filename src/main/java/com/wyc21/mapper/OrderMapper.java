package com.wyc21.mapper;

import com.wyc21.entity.Order;
import com.wyc21.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;
import com.wyc21.entity.OrderStatus;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单
     */
    void insert(Order order);

    /**
     * 根据ID查询订单
     */
    Order findById(String orderId);

    /**
     * 根据用户ID查询订单
     */
    List<Order> findByUserId(String userId);

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

    void updateOrderStatus(Map<String, Object> params);

    /**
     * 软删除订单
     */
    void softDeleteOrder(@Param("orderId") String orderId, @Param("modifiedUser") String modifiedUser);

    /**
     * 归档过期订单
     * 
     * @return 归档的订单数量
     */
    int archiveOrders();

    /**
     * 归档订单项
     * 
     * @return 归档的订单项数量
     */
    int archiveOrderItems();

    /**
     * 删除已归档的订单
     * 
     * @return 删除的订单数量
     */
    int deleteArchivedOrders();

    /**
     * 删除已归档的订单项
     * 
     * @return 删除的订单项数量
     */
    int deleteArchivedOrderItems();

    /**
     * 插入订单
     */
    void insertOrder(Order order);

    void batchInsertOrderItems(List<OrderItem> orderItems);
}