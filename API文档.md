# API 文档

## 基础说明

### 基础URL 
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
- categoryId: 分类ID（可选）
- keyword: 搜索关键词（可选）
- page: 页码，默认1
- size: 每页数量，默认10
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
"description": "最新款iPhone",
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

1. 所有需要认证的接口都需要在请求头中携带token
2. 管理员接口需要具有管理员权限
3. 分页接口的page从1开始计数
4. 时间格式统一使用：yyyy-MM-dd HH:mm:ss
5. 金额单位为元，保留两位小数