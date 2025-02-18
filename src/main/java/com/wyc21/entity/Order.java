package com.wyc21.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Order extends BaseEntity {
    private String orderId; // 订单号
    private String userId; // 用户ID
    private BigDecimal totalAmount; // 订单金额
    private OrderStatus status; // 订单状态
    private LocalDateTime createdTime; // 创建时间
    private LocalDateTime expireTime; // 过期时间
    private LocalDateTime payTime; // 支付时间
    private String paymentId; // 支付流水号
    private Integer version; // 版本号，用于乐观锁
    private String createdUser; // 创建者
    private String modifiedUser;
    private LocalDateTime modifiedTime;
    private List<OrderItem> items;
    private Boolean isDelete = false; // 默认为false，表示未删除
}