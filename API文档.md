# API 文档

## 基础说明

### 基础 URL

http://localhost:8080/api

### 通用返回格式

json
{
"code": 200, // 状态码
"message": "success", // 消息
"data": {} // 数据
}

### 通用状态码

- 200: 成功
- 400: 请求参数错误
- 401: 未授权
- 403: 权限不足
- 404: 资源不存在
- 500: 服务器错误

## 认证相关接口

### 1. 用户注册

http
POST /auth/register
请求体：
json
{
"username": "test_user",
"password": "password123",
"email": "test@example.com",
"phone": "13800138000"
}
成功响应：
json
{
"code": 200,
"message": "注册成功",
"data": null
}

### 2. 用户登录

http
POST /auth/login
请求体：
json
{
"username": "test_user",
"password": "password123"
}
成功响应：
json
{
"code": 200,
"message": "登录成功",
"data": {
"token": "eyJhbGciOiJIUzI1NiJ9...",
"user": {
"uid": 123456,
"username": "test_user",
"power": "user",
"email": "test@example.com"
}
}
}

## 管理员接口

### 1. 初始化数据库

http
POST /admin/init-database
请求头：
Authorization: Bearer {token}
成功响应：
json
{
"code": 200,
"message": "数据库初始化成功",
"data": null
}

### 2. 获取所有用户列表

http
GET /admin/users?page=1&size=10
请求头：
Authorization: Bearer {token}
成功响应：
json
{
"code": 200,
"message": "success",
"data": {
"total": 100,
"pages": 10,
"users": [
{
"uid": 123456,
"username": "test_user",
"email": "test@example.com",
"power": "user",
"createdTime": "2024-01-01 12:00:00"
}
]
}
}

# 商品相关接口

### 1. 获取商品分类

http
GET /categories

成功响应：
json
{
"code": 200,
"message": "success",
"data": [
{
"categoryId": 1,
"name": "电子产品",
"parentCategoryId": null,
"subCategories": [
{
"categoryId": 2,
"name": "手机",
"parentCategoryId": 1
}
]
}
]
}

### 2. 获取商品列表

http
GET /products?categoryId=1&keyword=手机&page=1&size=10

请求参数：

- categoryId: 分类 ID（可选）
- keyword: 搜索关键词（可选）
- page: 页码，默认 1
- size: 每页数量，默认 10
- sort: 排序方式（可选，price_asc/price_desc/rating_desc）

成功响应：
json
{
"code": 200,
"message": "success",
"data": {
"total": 100,
"pages": 10,
"products": [
{
"productId": 1,
"name": "iPhone 15",
"description": "最新款 iPhone",
"price": 6999.00,
"stock": 100,
"categoryId": 2,
"brand": "Apple",
"rating": 4.8,
"reviewCount": 520,
"imageUrl": "http://example.com/images/iphone15.jpg",
"tags": ["手机", "苹果", "新品"]
}
]
}
}

## 错误响应示例

### 1. 参数错误

json
{
"code": 400,
"message": "用户名或密码不能为空",
"data": null
}

### 2. 未授权

json
{
"code": 401,
"message": "请先登录",
"data": null
}

### 3. 权限不足

json
{
"code": 403,
"message": "需要管理员权限",
"data": null
}

## 注意事项

1. 所有需要认证的接口都需要在请求头中携带 token
2. 管理员接口需要具有管理员权限
3. 分页接口的 page 从 1 开始计数
4. 时间格式统一使用：yyyy-MM-dd HH:mm:ss
5. 金额单位为元，保留两位小数


## 购物车相关接口

### 1. 添加商品到购物车

http
POST /cart/items
请求头：
Authorization: Bearer {token}
请求体：
{
"productId": 1,
"quantity": 2
}
成功响应：
{
"code": 200,
"message": "添加成功",
"data": {
"cartItemId": 12345,
"productId": 1,
"productName": "iPhone 14",
"price": 6999.00,
"quantity": 2,
"createdTime": "2024-01-01 12:00:00"
}
}

### 2. 获取购物车列表
http
GET /cart/items
请求头：
Authorization: Bearer {token}
成功响应：
{
"code": 200,
"message": "success",
"data": {
"items": [
{
"cartItemId": 12345,
"productId": 1,
"productName": "iPhone 14",
"price": 6999.00,
"quantity": 2,
"createdTime": "2024-01-01 12:00:00"
}
],
"totalAmount": 13998.00
}
}

### 3. 更新购物车商品数量

http
PUT /cart/items/{cartItemId}
请求头：
Authorization: Bearer {token}
请求体：
{
"quantity": 3
}
成功响应：
{
"code": 200,
"message": "更新成功",
"data": {
"cartItemId": 12345,
"quantity": 3,
"price": 6999.00,
"totalPrice": 20997.00
}
}

### 4. 删除购物车商品
http
DELETE /cart/items/{cartItemId}
请求头：
Authorization: Bearer {token}
成功响应：
{
"code": 200,
"message": "删除成功",
"data": null
}

### 5. 清空购物车
http
DELETE /cart/items
请求头：
Authorization: Bearer {token}
成功响应：
{
"code": 200,
"message": "清空成功",
"data": null
}

## 订单相关接口

### 1. 创建订单

http
POST /orders
请求头：
Authorization: Bearer {token}
请求体：
{
"cartItemIds": [12345, 12346],
"address": {
"receiverName": "张三",
"phone": "13800138000",
"province": "广东省",
"city": "深圳市",
"district": "南山区",
"detailAddress": "科技园1号"
},
"paymentType": "ALIPAY" // ALIPAY/WECHAT/CREDIT_CARD
}
成功响应：
{
"code": 200,
"message": "订单创建成功",
"data": {
"orderId": "202401010001",
"totalAmount": 13998.00,
"status": "PENDING_PAYMENT",
"createdTime": "2024-01-01 12:00:00"
}
}

### 2. 获取订单列表
http
GET /orders?status=ALL&page=1&size=10
请求头：
Authorization: Bearer {token}
请求参数：
status: 订单状态（ALL/PENDING_PAYMENT/PAID/SHIPPED/COMPLETED/CANCELLED）
page: 页码，默认 1
size: 每页数量，默认 10
成功响应：
{
"code": 200,
"message": "success",
"data": {
"total": 100,
"pages": 10,
"orders": [
{
"orderId": "202401010001",
"totalAmount": 13998.00,
"status": "PENDING_PAYMENT",
"items": [
{
"productId": 1,
"productName": "iPhone 14",
"price": 6999.00,
"quantity": 2
}
],
"createdTime": "2024-01-01 12:00:00"
}
]
}
}

### 3. 获取订单详情
http
GET /orders/{orderId}
请求头：
Authorization: Bearer {token}
成功响应：
{
"code": 200,
"message": "success",
"data": {
"orderId": "202401010001",
"totalAmount": 13998.00,
"status": "PENDING_PAYMENT",
"items": [...],
"address": {...},
"paymentType": "ALIPAY",
"createdTime": "2024-01-01 12:00:00",
"paymentTime": null,
"shippingTime": null,
"completionTime": null
}
}

### 4. 取消订单
http
POST /orders/{orderId}/cancel
请求头：
Authorization: Bearer {token}
成功响应：
{
"code": 200,
"message": "订单已取消",
"data": null
}

## 错误响应补充

### 4. 库存不足
son
{
"code": 400,
"message": "商品库存不足",
"da
ta": null
}

### 5. 订单状态错误
json
{
"code": 400,
"message": "订单状态不允许此操作",
"data": null
}

## 注意事项补充

6. 购物车相关接口需要登录
7. 订单状态变更需要考虑状态流转的合法性
8. 创建订单时会检查商品库存
9. 订单号格式：yyyyMMddnnnn（年月日+4位序号）
10. 金额计算时注意精度问题，使用 BigDecimal