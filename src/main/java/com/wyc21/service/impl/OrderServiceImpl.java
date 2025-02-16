package com.wyc21.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import com.wyc21.entity.Order;
import com.wyc21.entity.OrderStatus;
import com.wyc21.entity.CartItem;
import com.wyc21.mapper.OrderMapper;
import com.wyc21.service.IOrderService;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.math.BigDecimal;
import com.wyc21.entity.OrderItem;
import com.wyc21.service.IIdGenerator;
import com.wyc21.entity.User;
import com.wyc21.mapper.UserMapper;
import com.wyc21.entity.Product;
import com.wyc21.mapper.ProductMapper;
import com.wyc21.service.ex.ProductNotFoundException;
import com.wyc21.service.ex.InsuffientStockException;
import com.wyc21.service.ex.UserNotFoundException;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Propagation;
import com.wyc21.service.ex.OrderStatusException;
import com.wyc21.service.ex.OrderExpiredException;
import com.wyc21.service.ex.OrderNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Collections;
import com.wyc21.service.ex.AccessDeniedException;

@Service
@Slf4j
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private IIdGenerator idGenerator;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    // 订单过期时间
    private static final long ORDER_EXPIRE_MINUTES = 30;

    // 添加新方法：直接购买单个商品
    @Override
    @Transactional
    public Order createOrderDirect(Long userId, Long productId, Integer quantity) {
        // 创建一个临时的 CartItem 来复用现有逻辑
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new ProductNotFoundException("商品不存在");
        }

        CartItem item = new CartItem();
        item.setProductId(String.valueOf(productId));
        item.setQuantity(quantity);
        item.setPrice(product.getPrice());
        item.setProductName(product.getName());

        List<CartItem> items = Collections.singletonList(item);
        return createOrder(userId, items);
    }

    @Override
    @Transactional
    public Order createOrder(Long userId, List<CartItem> items) {
        // 1. 验证用户
        User user = userMapper.findByUid(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }

        // 2. 验证商品库存
        for (CartItem item : items) {
            Product product = productMapper.findById(Long.parseLong(item.getProductId()));
            if (product == null) {
                throw new ProductNotFoundException("商品不存在：" + item.getProductId());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new InsuffientStockException("商品库存不足：" + product.getName());
            }
        }

        // 3. 计算订单总金额
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 创建订单
        Order order = new Order();
        String orderId = generateOrderId();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedTime(LocalDateTime.now());
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));
        order.setVersion(1); // 初始版本号
        order.setCreatedUser("system");
        order.setModifiedTime(LocalDateTime.now());
        order.setModifiedUser("system");

        // 5. 保存订单
        orderMapper.insert(order);

        // 6. 保存订单商品并扣减库存
        for (CartItem item : items) {
            // 创建订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderItemId(idGenerator.nextId());
            orderItem.setOrderId(order.getOrderId());
            orderItem.setProductId(Long.parseLong(item.getProductId()));
            orderItem.setProductName(item.getProductName());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            orderItem.setCreatedUser("system");
            orderItem.setModifiedTime(LocalDateTime.now());
            orderItem.setModifiedUser("system");
            orderMapper.insertOrderItem(orderItem);

            // 扣减库存
            productMapper.decreaseStock(Long.parseLong(item.getProductId()), item.getQuantity());
        }

        // 7. 更新订单状态为待支付
        order.setStatus(OrderStatus.PENDING_PAY);
        // 不要手动修改版本号，由数据库管理
        orderMapper.updateOrder(order);

        // 8. 设置Redis过期时间
        String orderKey = "order:" + order.getOrderId();
        redisTemplate.opsForValue().set(orderKey, order.getStatus().name(),
                ORDER_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return order;
    }

    @Override
    @Transactional
    public boolean payOrder(String orderId, String paymentId) {
        // 从 Redis 获取订单状态
        String orderKey = "order:" + orderId;
        String status = redisTemplate.opsForValue().get(orderKey);

        // 如果 Redis 中没有状态，则从数据库中查询
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            log.error("订单不存在: {}", orderId);
            throw new OrderNotFoundException("订单不存在：" + orderId);
        }

        log.info("从Redis获取的订单状态: {}", status);
        log.info("从数据库获取的订单状态: {}", order.getStatus().name());
        log.info("当前版本号: {}", order.getVersion());

        // 如果 Redis 返回 null，检查订单是否过期
        if (status == null) {
            // 再次检查订单的过期时间
            if (order.getExpireTime().isBefore(LocalDateTime.now())) {
                // 更新订单状态为过期
                order.setStatus(OrderStatus.EXPIRED);
                // order.setVersion(order.getVersion() + 1);
                order.setModifiedTime(LocalDateTime.now());
                order.setModifiedUser("system");

                // 更新数据库
                orderMapper.updateOrder(order);

                // 恢复库存
                List<OrderItem> orderItems = orderMapper.findOrderItems(orderId);
                for (OrderItem item : orderItems) {
                    productMapper.increaseStock(item.getProductId(), item.getQuantity());
                }

                // 删除Redis中的key
                redisTemplate.delete(orderKey);

                throw new OrderExpiredException("订单已过期");
            }
        } else {
            // 检查订单状态
            if (!OrderStatus.PENDING_PAY.name().equals(status)) {
                throw new OrderStatusException("订单状态不正确");
            }
        }

        // 更新订单状态
        order.setStatus(OrderStatus.PAID);
        order.setPaymentId(paymentId);
        order.setPayTime(LocalDateTime.now());
        order.setModifiedTime(LocalDateTime.now());
        order.setModifiedUser("system");
        // 不要手动修改版本号

        int updated = orderMapper.updateOrder(order);
        if (updated > 0) {
            orderKey = "order:" + orderId;
            redisTemplate.opsForValue().set(orderKey, OrderStatus.PAID.name());
            return true;
        }
        return false;
    }

    @Override
    @Scheduled(fixedDelay = 1000000) // 每10分钟检查一次
    @Transactional
    public void checkExpiredOrders() {
        // 检查数据库是否已初始化!isDatabaseInitialized()
        if (!isDatabaseInitialized()) {
            log.warn("数据库尚未初始化，跳过过期订单检查");
            return;
        }
        // 获取所有过期未支付的订单
        List<Order> expiredOrders = orderMapper.findExpiredOrders(LocalDateTime.now());
        log.info("找到{}个过期订单", expiredOrders.size());

        for (Order order : expiredOrders) {
            try {
                log.info("处理过期订单，订单ID: {}, 当前版本号: {}", order.getOrderId(), order.getVersion());

                // 更新订单状态为过期
                order.setStatus(OrderStatus.EXPIRED);

                order.setModifiedTime(LocalDateTime.now());
                order.setModifiedUser("system");

                // 更新数据库
                int updated = orderMapper.updateOrder(order);
                if (updated > 0) {
                    log.info("订单状态已更新为过期: {}", order.getOrderId());

                    // 恢复库存
                    List<OrderItem> orderItems = orderMapper.findOrderItems(order.getOrderId());
                    for (OrderItem item : orderItems) {
                        productMapper.increaseStock(item.getProductId(), item.getQuantity());
                    }

                    // 删除Redis中的key
                    String orderKey = "order:" + order.getOrderId();
                    redisTemplate.delete(orderKey);

                    log.info("订单{}已过期，库存已恢复", order.getOrderId());
                } else {
                    log.error("更新订单状态失败: {}", order.getOrderId());
                }
            } catch (Exception e) {
                log.error("处理过期订单失败: {}", order.getOrderId(), e);
            }
        }
    }

    @Override
    @Transactional
    public void cancelOrder(String orderId) {
        // 保留旧方法的实现，但标记为过时
        cancelOrder(orderId, null);
    }

    @Override
    @Transactional
    public void cancelOrder(String orderId, Long userId) {
        // 1. 查询订单并检查
        Order order = orderMapper.findById(orderId);
        if (order == null || Boolean.TRUE.equals(order.getIsDelete())) {
            throw new OrderNotFoundException("订单不存在");
        }

        // 2. 检查权限
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new AccessDeniedException("无权操作此订单");
        }

        // 3. 检查订单状态
        OrderStatus status = order.getStatus();

        // 4. 软删除订单（设置 is_delete = 1）
        orderMapper.softDeleteOrder(orderId, "system");

        // 5. 只有创建状态或待支付状态的订单需要恢复库存
        if (status == OrderStatus.CREATED || status == OrderStatus.PENDING_PAY) {
            List<OrderItem> orderItems = orderMapper.findOrderItems(orderId);
            for (OrderItem item : orderItems) {
                productMapper.increaseStock(item.getProductId(), item.getQuantity());
            }
        }

        // 6. 删除 Redis 中的订单状态
        String orderKey = "order:" + orderId;
        redisTemplate.delete(orderKey);
    }

    private String generateOrderId() {
        // 生成订单号：年月日时分秒+6位随机数
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStr = sdf.format(new Date());
        String randomStr = String.format("%06d", new Random().nextInt(1000000));
        return timeStr + randomStr;
    }

    private boolean doPayment(String paymentId, BigDecimal amount) {
        // 这里应该调用实际的支付接口
        // 为了演示，我们模拟支付过程
        try {
            Thread.sleep(1000); // 模拟支付耗时
            return true; // 模拟支付成功
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public Order getOrder(String orderId) {
        return orderMapper.findById(orderId);
    }

    private boolean isDatabaseInitialized() {
        try {
            // 查询 wz_users 表是否存在数据
            String sql = "SELECT COUNT(*) FROM wz_users";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null && count > 0; // 如果表存在且有数据，返回 true
        } catch (Exception e) {
            log.warn("数据库未初始化或表不存在: {}", e.getMessage());
            return false; // 如果查询失败，返回 false
        }
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        // 获取用户的所有订单
        List<Order> orders = orderMapper.findByUserId(userId);

        // 为每个订单加载订单项
        for (Order order : orders) {
            List<OrderItem> items = orderMapper.findOrderItems(order.getOrderId());
            order.setItems(items);
        }

        // 按创建时间降序排序（最新的订单在前）
        orders.sort((o1, o2) -> o2.getCreatedTime().compareTo(o1.getCreatedTime()));

        return orders;
    }
}