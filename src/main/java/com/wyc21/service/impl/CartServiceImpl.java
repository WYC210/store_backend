package com.wyc21.service.impl;

import com.wyc21.entity.Cart;
import com.wyc21.entity.CartItem;
import com.wyc21.entity.Product;
import com.wyc21.entity.User;
import com.wyc21.service.ICartService;
import com.wyc21.service.ex.CartNotFoundException;
import com.wyc21.service.ex.ProductNotFoundException;
import com.wyc21.service.ex.UserNotFoundException;
import com.wyc21.mapper.CartMapper;
import com.wyc21.mapper.ProductMapper;
import com.wyc21.mapper.UserMapper;
import com.wyc21.util.JsonResult;
import com.wyc21.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wyc21.entity.Order;
import com.wyc21.entity.OrderItem;
import com.wyc21.entity.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Date;
import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.Random;
import com.wyc21.mapper.OrderMapper;
import com.wyc21.service.ex.InsuffientStockException;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
import com.wyc21.service.ex.InsertException;
// 导入 Map 和 HashMap
import java.util.Map;
import java.util.HashMap;
import com.wyc21.util.JsonResult; // 导入 JsonResult 类
import java.util.ArrayList;

@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SnowflakeIdGenerator idGenerator;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final long ORDER_EXPIRE_MINUTES = 30;

    @Override
    @Transactional
    public JsonResult<Map<String, Object>> purchaseProduct(Long userId, String productId, Integer quantity) {
        // 1. 检查用户是否存在
        User user = userMapper.findByUid(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }

        // 2. 检查商品是否存在
        Product product = productMapper.findById(Long.parseLong(productId));
        if (product == null) {
            throw new ProductNotFoundException("商品不存在");
        }

        // 3. 检查库存
        if (product.getStock() < quantity) {
            throw new InsuffientStockException("商品库存不足");
        }

        // 4. 创建订单
        Order order = new Order();
        String orderId = generateOrderId(); // 生成订单ID
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setTotalAmount(product.getPrice().multiply(new BigDecimal(quantity))); // 计算总金额
        order.setStatus(OrderStatus.CREATED); // 设置订单状态为创建
        order.setVersion(1); // 设置初始版本号
        order.setCreatedTime(LocalDateTime.now());
        order.setExpireTime(LocalDateTime.now().plusMinutes(30)); // 设置过期时间
        order.setCreatedUser(user.getUsername());
        order.setModifiedTime(LocalDateTime.now());
        order.setModifiedUser(user.getUsername());

        // 5. 保存订单
        orderMapper.insert(order);

        // 6. 创建订单项
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(idGenerator.nextId()); // 生成订单项ID
        orderItem.setOrderId(orderId);
        orderItem.setProductId(Long.parseLong(productId));
        orderItem.setProductName(product.getName());
        orderItem.setQuantity(quantity);
        orderItem.setPrice(product.getPrice());
        orderItem.setCreatedUser(user.getUsername());
        orderItem.setModifiedTime(LocalDateTime.now());
        orderItem.setModifiedUser(user.getUsername());

        // 7. 保存订单项
        orderMapper.insertOrderItem(orderItem);

        // 8. 扣减库存
        productMapper.decreaseStock(Long.parseLong(productId), quantity);

        // 9. 更新订单状态为待支付
        order.setStatus(OrderStatus.PENDING_PAY);
        // 不要手动修改版本号，由数据库管理
        orderMapper.updateOrder(order);

        // 10. 设置Redis过期时间
        String orderKey = "order:" + order.getOrderId();
        redisTemplate.opsForValue().set(orderKey, order.getStatus().name(),
                ORDER_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 11. 返回购买的商品信息
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("orderId", orderId); // 订单ID
        responseData.put("totalAmount", order.getTotalAmount()); // 总金额

        // 明确指定泛型类型
        // 在 CartServiceImpl.java 中
        return new JsonResult<>(200, responseData, "购买成功"); // 正确的参数顺序：state, data, message
    }

    private String generateOrderId() {
        // 生成订单号：年月日时分秒+6位随机数
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStr = sdf.format(new Date());
        String randomStr = String.format("%06d", new Random().nextInt(1000000));
        return timeStr + randomStr;
    }

    @Override
    @Transactional
    public CartItem addToCart(Long userId, String productId, Integer quantity) {
        // 检查用户是否存在
        User user = userMapper.findByUid(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }

        // 检查商品是否存在
        Product product = productMapper.findById(Long.parseLong(productId));
        if (product == null) {
            throw new ProductNotFoundException("商品不存在");
        }

        // 检查库存
        if (product.getStock() < quantity) {
            throw new ProductNotFoundException("商品库存不足");
        }

        // 检查购物车是否存在
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setCartId(idGenerator.nextId());
            cart.setUserId(userId);
            cart.setCreatedUser("system");
            cartMapper.insert(cart);
        }

        // 检查商品是否已在购物车中
        CartItem existingItem = cartMapper.findCartItem(cart.getCartId(), Long.parseLong(productId));
        if (existingItem != null) {
            // 直接更新为新的数量，而不是相加
            existingItem.setQuantity(quantity);
            cartMapper.updateCartItem(existingItem);
            return existingItem;
        } else {
            // 添加新商品
            CartItem newItem = new CartItem();
            newItem.setCartItemId(String.valueOf(idGenerator.nextId()));
            newItem.setCartId(String.valueOf(cart.getCartId()));
            newItem.setProductId(String.valueOf(Long.parseLong(productId)));
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getPrice());
            newItem.setProductName(product.getName());
            newItem.setCreatedUser("system");
            cartMapper.insertCartItem(newItem);
            return newItem;
        }
    }

    @Override
    public List<CartItem> getCartItems(Long userId) {
        // 检查用户是否存在
        User user = userMapper.findByUid(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }

        // 获取购物车项
        List<CartItem> items = cartMapper.findCartItems(userId);

        return items != null ? items : new ArrayList<>();
    }

    @Override
    public CartItem getCartItem(Long userId, String cartItemId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            throw new CartNotFoundException("购物车不存在");
        }
        // CartItem item = cartMapper.findCartItemById(String.valueOf(cartItemId));
        // if (item == null || !item.getCartId().equals(cart.getCartId())) {

        CartItem item = cartMapper.findCartItemById(cartItemId);
        if (item == null || !item.getCartId().equals(String.valueOf(cart.getCartId()))) {
            throw new CartNotFoundException("购物车商品不存在");
        }
        return item;
    }

    @Override
    @Transactional
    public CartItem updateQuantity(Long userId, String cartItemId, Integer quantity) {
        CartItem item = getCartItem(userId, cartItemId);

        // 检查库存
        Product product = productMapper.findById(Long.parseLong(item.getProductId()));
        if (product.getStock() < quantity) {
            throw new ProductNotFoundException("商品库存不足");
        }

        item.setQuantity(quantity);
        cartMapper.updateCartItem(item);
        return item;
    }

    @Override
    @Transactional
    public void deleteCartItem(Long userId, String cartItemId) {
        CartItem item = cartMapper.findCartItemById(cartItemId);

        if (item == null) {
            throw new CartNotFoundException("购物车商品不存在");
        }

        // 验证购物车项是否属于当前用户
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null || !item.getCartId().equals(String.valueOf(cart.getCartId()))) {
            throw new CartNotFoundException("无权操作此购物车商品");
        }

        // 验证通过，执行删除
        cartMapper.deleteCartItem(cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart != null) {
            cartMapper.deleteAllCartItems(cart.getCartId());
        }
    }

    @Override
    public BigDecimal getCartTotal(Long userId) {
        List<CartItem> items = getCartItems(userId);
        return items.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}