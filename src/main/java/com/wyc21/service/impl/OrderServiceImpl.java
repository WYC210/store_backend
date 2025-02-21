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
import java.util.Map;
import java.util.HashMap;
import com.wyc21.util.JsonResult;
import com.wyc21.service.ICartService;
import com.wyc21.service.ex.CartNotFoundException;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
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

    @Autowired
    private ICartService cartService;

    // 订单过期时间
    private static final long ORDER_EXPIRE_MINUTES = 30;

    @Override
    @Transactional
    public Order createOrderDirect(String userId, String productId, Integer quantity) {
        // 获取商品信息
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new ProductNotFoundException("商品不存在");
        }

        // 计算总金额
        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(quantity));

        // 创建订单对象
        Order order = new Order();
        order.setOrderId(idGenerator.nextId().toString());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount); // 确保这里设置了总金额
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setCreatedTime(LocalDateTime.now());
        order.setExpireTime(LocalDateTime.now().plusMinutes(30)); // 设置过期时间
        order.setVersion(1);
        order.setCreatedUser(userId);

        order.setModifiedTime(LocalDateTime.now());

        // 插入订单到数据库
        orderMapper.insert(order);
        return order;
    }

    @Override
    @Transactional
    public Order createOrderFromCart(String userId, List<String> cartItemIds) {
        // 验证用户
        User user = userMapper.findByUid(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }
        
        // 获取购物车商品
        List<CartItem> cartItems = cartService.getCartItemsByIds(userId, cartItemIds);
        if (cartItems.isEmpty()) {
            throw new CartNotFoundException("未找到选中的商品");
        }

        // 创建订单
        Order order = createOrderFromItems(userId, cartItems);

        // // 从购物车中删除已购买的商品
        // for (String cartItemId : cartItemIds) {
        //     cartService.deleteCartItem(userId, cartItemId);
        // }

        return order;
    }

    private Product validateProduct(String productId, Integer quantity) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new ProductNotFoundException("商品不存在");
        }
        if (product.getStock() < quantity) {
            throw new InsuffientStockException("商品库存不足");
        }
        return product;
    }

    private Order createOrder(String userId) {
        Order order = new Order();
        order.setOrderId(idGenerator.nextId().toString());
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setCreatedTime(LocalDateTime.now());
        order.setExpireTime(LocalDateTime.now().plusMinutes(ORDER_EXPIRE_MINUTES));
        return order;
    }

    private OrderItem createOrderItem(String orderId, Product product, Integer quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(idGenerator.nextId().toString());
        orderItem.setOrderId(orderId);
        orderItem.setProductId(String.valueOf(product.getProductId()));
        orderItem.setProductName(product.getName());
        orderItem.setQuantity(quantity);
        orderItem.setPrice(product.getPrice());
        orderItem.setCreatedTime(LocalDateTime.now());
        return orderItem;
    }

    private void saveOrder(Order order, OrderItem orderItem) {
        orderMapper.insert(order);
        orderMapper.insertOrderItem(orderItem);
    }

    private void saveOrder(Order order, List<OrderItem> orderItems) {
        orderMapper.insert(order);
        orderMapper.batchInsertOrderItems(orderItems);
    }

    private void processCartItems(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            decreaseStock(item.getProductId(), item.getQuantity());
            cartService.deleteCartItem(item.getCartId(), item.getCartItemId());
        }
    }

    @Override
    @Transactional
    public Order createOrder(String userId, List<CartItem> items) {
        // 1. 验证用户
        User user = userMapper.findByUid(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }

        // 2. 验证商品库存
        for (CartItem item : items) {
            Product product = productMapper.findById(item.getProductId());
            if (product == null) {
                throw new ProductNotFoundException("商品不存在：" + item.getProductId());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new InsuffientStockException("商品库存不足：" + product.getName());
            }

            // 使用前端传来的价格，但需要验证价格是否正确
            log.info("验证商品价格: 商品ID = {}, 数据库价格 = {}, 前端价格 = {}",
                    item.getProductId(), product.getPrice(), item.getPrice());

            if (!product.getPrice().equals(item.getPrice())) {
                log.error("商品价格不一致: 商品ID = {}, 数据库价格 = {}, 前端价格 = {}",
                        item.getProductId(), product.getPrice(), item.getPrice());
                throw new ProductNotFoundException("商品价格有变化，请刷新后重试");
            }
        }

        // 3. 计算订单总金额（只计算选中的商品）
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
        order.setVersion(1);
        order.setCreatedUser(user.getUsername());
        order.setModifiedTime(LocalDateTime.now());
        order.setModifiedUser(user.getUsername());

        // 5. 保存订单
        orderMapper.insert(order);

        // 6. 保存订单商品并扣减库存
        for (CartItem item : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderItemId(idGenerator.nextId().toString());
            orderItem.setOrderId(order.getOrderId());
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            orderItem.setCreatedUser(user.getUsername());
            orderItem.setModifiedTime(LocalDateTime.now());
            orderItem.setModifiedUser(user.getUsername());
            orderItem.setCreatedTime(LocalDateTime.now());
            orderItem.setModifiedTime(LocalDateTime.now());
            orderMapper.insertOrderItem(orderItem);

            // 扣减库存
            productMapper.decreaseStock(item.getProductId(), item.getQuantity());
        }

        // 7. 更新订单状态为待支付
        order.setStatus(OrderStatus.PENDING_PAY);
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("status", OrderStatus.PENDING_PAY.name());
        params.put("modifiedUser", user.getUsername());
        orderMapper.updateOrderStatus(params);

        // 8. 设置Redis过期时间
        String orderKey = "order:" + order.getOrderId();
        redisTemplate.opsForValue().set(orderKey, OrderStatus.PENDING_PAY.name(),
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

        // 获取订单项并更新购物车已支付数量
        List<OrderItem> orderItems = orderMapper.findOrderItems(orderId);
        if (orderItems != null && !orderItems.isEmpty()) {
            for (OrderItem item : orderItems) {
                cartService.updateCartItemPaid_quantity(
                        order.getUserId(),
                        item.getProductId(),
                        item.getQuantity());
            }
        }

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
    public void cancelOrder(String orderId, String userId) {
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

        // 4. 软删除订单
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
    public List<Order> getOrdersByUserId(String userId) {
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

    @Override
    public void updateOrderStatus(String orderId, String status, String modifiedUser) {
        // Logic to update the order status
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("status", status);
        params.put("modifiedUser", modifiedUser);

        orderMapper.updateOrderStatus(params);
    }

    @Override
    @Transactional
    public JsonResult<Map<String, Object>> purchaseProduct(String userId, List<CartItem> items) {
        try {
            // 创建订单
            Order order = createOrder(userId, items);

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getOrderId());
            result.put("totalAmount", order.getTotalAmount());
            result.put("status", order.getStatus());

            return new JsonResult<>(200, result, "订单创建成功");
        } catch (Exception e) {
            log.error("购买商品失败: ", e);
            return new JsonResult<>(500, null, "购买失败：" + e.getMessage());
        }
    }

    private Order createOrderFromItems(String userId, List<CartItem> items) {
        // 创建订单
        Order order = new Order();
        String orderId = String.valueOf(idGenerator.nextId());
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING_PAY); // 直接设置枚举值

        // 计算总金额
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);

        // 设置创建时间和创建用户
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedTime(now);
        order.setModifiedTime(now);
        String username = userMapper.findByUid(userId).getUsername();
        order.setCreatedUser(username);
        order.setModifiedUser(username);
       
        // 保存订单
        orderMapper.insert(order);

        // 创建订单项
        for (CartItem item : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderItemId(idGenerator.nextId().toString());
            orderItem.setOrderId(orderId);
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity() - item.getPaidQuantity());
            orderItem.setPrice(item.getPrice());
            orderItem.setProductName(item.getProductName());
            orderItem.setCreatedUser(username);
            orderItem.setCreatedTime(now);
            orderItem.setModifiedUser(username);
            orderItem.setModifiedTime(now);

            orderMapper.insertOrderItem(orderItem);

            // 扣减库存
            productMapper.decreaseStock(item.getProductId(), item.getQuantity());
        }

        // 设置订单过期时间
        String orderKey = "order:" + orderId;
        redisTemplate.opsForValue().set(orderKey, OrderStatus.PENDING_PAY.name(),
                ORDER_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return order;
    }

    private void decreaseStock(String productId, Integer quantity) {
        productMapper.decreaseStock(productId, quantity);
    }

    private void validateCartItems(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            Product product = productMapper.findById(item.getProductId());
            if (product == null) {
                throw new ProductNotFoundException("商品不存在: " + item.getProductId());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new InsuffientStockException("商品库存不足: " + product.getName());
            }
        }
    }

    private List<OrderItem> createOrderItemsFromCart(String orderId, List<CartItem> cartItems) {
        return cartItems.stream().map(item -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderItemId(idGenerator.nextId().toString());
            orderItem.setOrderId(orderId);
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            orderItem.setCreatedTime(LocalDateTime.now());
            orderItem.setCreatedUser("system");
            return orderItem;
        }).collect(Collectors.toList());
    }

    private OrderItem createOrderItem(String orderId, CartItem item) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(idGenerator.nextId().toString());
        orderItem.setOrderId(orderId);
        orderItem.setProductId(item.getProductId());
        orderItem.setProductName(item.getProductName());
        orderItem.setQuantity(item.getQuantity());
        orderItem.setPrice(item.getPrice());
        orderItem.setCreatedTime(LocalDateTime.now());
        return orderItem;
    }
}