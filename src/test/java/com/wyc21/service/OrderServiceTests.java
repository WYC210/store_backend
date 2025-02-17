package com.wyc21.service;

import com.wyc21.entity.Order;
import com.wyc21.entity.OrderStatus;
import com.wyc21.entity.CartItem;
import com.wyc21.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

import com.wyc21.service.ex.UserNotFoundException;

import com.wyc21.service.ex.InsuffientStockException;

import com.wyc21.mapper.ProductMapper;

import org.springframework.data.redis.core.RedisTemplate;

import com.wyc21.service.ex.OrderExpiredException;
import org.springframework.transaction.annotation.Propagation;
@Slf4j
@SpringBootTest

public class OrderServiceTests {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private ICartService cartService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private DatabaseInitService databaseInitService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    

    @BeforeEach
    void setUp() {
        databaseInitService.initializeDatabase();
    }

    @Test
    public void testCreateOrder() {
        log.info("开始测试创建订单");

        // 准备测试数据
        Long userId = 2L;
        List<CartItem> items = new ArrayList<>();

        // 添加商品到购物车
        CartItem item1 = cartService.addToCartWithCheck(userId, String.valueOf(1L), 1); // iPhone 14
        CartItem item2 = cartService.addToCartWithCheck(userId, String.valueOf(2L), 2); // MacBook Pro
        items.add(item1);
        items.add(item2);

        // 创建订单
        Order order = orderService.createOrder(userId, items);

        // 验证订单状态
        assertEquals(OrderStatus.PENDING_PAY, order.getStatus(), "订单状态应为PENDING_PAY");
    }

    @Test
    public void testPayOrder() {
        log.info("开始测试支付订单");

        // 创建订单
        Long userId = 2L;
        List<CartItem> items = new ArrayList<>();
        CartItem item = cartService.addToCartWithCheck(userId, String.valueOf(1L), 1);
        items.add(item);

        // 确保订单创建成功
        Order order = orderService.createOrder(userId, items);
        assertNotNull(order, "订单创建失败");

        // 输出订单状态以便检查
        log.info("订单创建后状态: {}", order.getStatus()); // 输出订单状态

        // 支付订单
        String paymentId = "PAY" + System.currentTimeMillis();
        boolean result = orderService.payOrder(order.getOrderId(), paymentId);

        // 验证支付结果
        assertTrue(result, "支付应该成功");

        // 验证订单状态
        Order paidOrder = orderService.getOrder(order.getOrderId());
        assertEquals(OrderStatus.PAID, paidOrder.getStatus(), "订单状态应为已支付");
        assertEquals(paymentId, paidOrder.getPaymentId(), "支付ID应匹配");

        log.info("订单支付成功");
    }

    @Test
    public void testPayExpiredOrder() {
        // 1. 创建订单
        Long userId = 2L;
        List<CartItem> items = new ArrayList<>();
        CartItem item = cartService.addToCartWithCheck(userId, String.valueOf(1L), 1);
        items.add(item);

        Order order = orderService.createOrder(userId, items);
        assertNotNull(order);
        String orderId = order.getOrderId();

        // 记录原始库存
        Product product = productMapper.findById(1L);
        int originalStock = product.getStock();

        // 2. 等待订单过期
        try {
            log.info("等待订单过期...");
            Thread.sleep(70000); // 等待70秒，确保订单过期
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("测试被中断");
        }

        // 3. 尝试支付过期订单
        try {
            orderService.payOrder(orderId, "test_payment_id");
            fail("预期应该抛出OrderExpiredException异常");
        } catch (OrderExpiredException e) {
            // 4. 验证订单状态
            Order updatedOrder = orderService.getOrder(orderId);
            assertNotNull(updatedOrder);
            assertEquals(OrderStatus.EXPIRED, updatedOrder.getStatus());

            // 5. 验证库存是否已恢复
            product = productMapper.findById(1L);
            assertEquals(originalStock, product.getStock());

            // 6. 验证Redis缓存是否已删除
            String status = redisTemplate.opsForValue().get("order:" + orderId);
            assertNull(status);
        }
    }

    @Test
    public void testCancelOrder() {
        log.info("开始测试取消订单");

        // 创建订单
        Long userId = 2L;
        List<CartItem> items = new ArrayList<>();
        CartItem item = cartService.addToCartWithCheck(userId, String.valueOf(1L), 1);
        items.add(item);
        Order order = orderService.createOrder(userId, items);

        // 取消订单
        orderService.cancelOrder(order.getOrderId());

        // 验证订单状态
        Order cancelledOrder = orderService.getOrder(order.getOrderId());
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus(), "订单状态应为CANCELLED");

        log.info("订单取消成功");
    }

    @Test
    public void testConcurrentPayment() {
        log.info("开始测试并发支付");

        // 创建订单
        Long userId = 2L;
        List<CartItem> items = new ArrayList<>();
        CartItem item = cartService.addToCartWithCheck(userId, String.valueOf(1L), 1);
        items.add(item);
        Order order = orderService.createOrder(userId, items);

        // 模拟并发支付
        String paymentId1 = "PAY1" + System.currentTimeMillis();
        String paymentId2 = "PAY2" + System.currentTimeMillis();

        // 创建两个线程同时支付
        Thread thread1 = new Thread(() -> {
            orderService.payOrder(order.getOrderId(), paymentId1);
        });

        Thread thread2 = new Thread(() -> {
            orderService.payOrder(order.getOrderId(), paymentId2);
        });

        // 启动线程
        thread1.start();
        thread2.start();

        // 等待线程结束
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 验证订单状态
        Order paidOrder = orderService.getOrder(order.getOrderId());
        assertEquals(OrderStatus.PAID, paidOrder.getStatus(), "订单状态应为PAID");
        assertNotNull(paidOrder.getPaymentId(), "支付ID不应为null");

        log.info("并发支付测试完成");
    }

    @Test
    public void testCreateOrderWithInsufficientStock() {
        log.info("开始测试库存不足场景");

        Long userId = 2L;
        List<CartItem> items = new ArrayList<>();

        // 添加超出库存数量的商品
        CartItem item = cartService.addToCartWithCheck(userId, String.valueOf(1L), 999999);
        items.add(item);

        // 验证是否抛出异常
        assertThrows(InsuffientStockException.class, () -> {
            orderService.createOrder(userId, items);
        });

        log.info("库存不足测试完成");
    }

    @Test
    public void testCreateOrderWithInvalidUser() {
        log.info("开始测试无效用户场景");

        Long invalidUserId = 99999L;
        List<CartItem> items = new ArrayList<>();

        // 验证是否抛出异常
        assertThrows(UserNotFoundException.class, () -> {
            orderService.createOrder(invalidUserId, items);
        });

        log.info("无效用户测试完成");
    }

    @Test
    public void testOrderExpiration() {
        log.info("开始测试订单过期场景");

        // 创建订单
        Long userId = 2L;
        List<CartItem> items = new ArrayList<>();
        CartItem item = cartService.addToCartWithCheck(userId, String.valueOf(1L), 1);
        items.add(item);

        Order order = orderService.createOrder(userId, items);

        // 获取初始库存
        Product product = productMapper.findById(1L);
        int initialStock = product.getStock();

        // 等待订单过期检查
        try {
            Thread.sleep(2000); // 等待2秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 手动触发过期检查
        orderService.checkExpiredOrders();

        // 验证订单状态
        Order expiredOrder = orderService.getOrder(order.getOrderId());
        assertEquals(OrderStatus.EXPIRED, expiredOrder.getStatus(), "订单应该已过期");

        // 验证库存已恢复
        product = productMapper.findById(1L);
        assertEquals(initialStock, product.getStock(), "库存应该已恢复");

        log.info("订单过期测试完成");
    }

    @Test
    public void testCancelOrderWithStockRecovery() {
        log.info("开始测试取消订单并恢复库存");

        // 获取初始库存
        Long productId = 1L;
        Product product = productMapper.findById(productId);
        int initialStock = product.getStock();

        // 创建订单
        Long userId = 2L;
        List<CartItem> items = new ArrayList<>();
        CartItem item = cartService.addToCartWithCheck(userId, String.valueOf(productId), 2);
        items.add(item);
        Order order = orderService.createOrder(userId, items);

        // 验证库存已扣减
        product = productMapper.findById(productId);
        assertEquals(initialStock - 2, product.getStock(), "库存应该已扣减");

        // 取消订单
        orderService.cancelOrder(order.getOrderId());

        // 验证库存已恢复
        product = productMapper.findById(productId);
        assertEquals(initialStock, product.getStock(), "库存应该已恢复");

        log.info("取消订单并恢复库存测试完成");
    }
}