package com.wyc21.entity;

public enum OrderStatus {
    CREATED, // 订单创建
    PENDING_PAY, // 待支付
    PAYING, // 支付中
    PAID, // 已支付
    CANCELLED, // 已取消
    EXPIRED // 已过期
}