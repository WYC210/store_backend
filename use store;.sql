use store;

/* -- 更改字段名
ALTER TABLE t_user CHANGE salt power VARCHAR(255); */
/* -- 创建用户表
CREATE TABLE wz_users (
    uid INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password CHAR(60) NOT NULL,
    power VARCHAR(20),  -- 用户级别或角色
    phone VARCHAR(20),
    email VARCHAR(30),
    gender INT,
    avatar VARCHAR(50),
    is_delete INT DEFAULT 0,  -- 0表示未删除，1表示已删除
    created_user VARCHAR(20),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_user VARCHAR(20),
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建类别表
CREATE TABLE wz_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    parent_category_id INT,  -- 父类别ID，用于多级分类
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建商品表
CREATE TABLE wz_products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    category_id INT,
    sku VARCHAR(50),  -- 库存单位
    brand VARCHAR(100),  -- 品牌
    tags VARCHAR(255),  -- 以逗号分隔的标签
    rating DECIMAL(3, 2) DEFAULT 0,  -- 商品评分
    review_count INT DEFAULT 0,  -- 评论数量
    low_stock_threshold INT DEFAULT 10,  -- 库存预警阈值
    is_active TINYINT DEFAULT 1,  -- 1表示上架，0表示下架
    image_url VARCHAR(255),  -- 商品主图URL
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES wz_categories(category_id)
);

-- 创建订单表
CREATE TABLE wz_orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    total_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('pending', 'completed', 'canceled') DEFAULT 'pending',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES wz_users(uid)
);

-- 创建订单项表
CREATE TABLE wz_order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    product_id INT,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES wz_orders(order_id),
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id)
);

-- 创建购物车表
CREATE TABLE wz_carts (
    cart_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_checked_out TINYINT DEFAULT 0,  -- 0表示未结算，1表示已结算
    FOREIGN KEY (user_id) REFERENCES wz_users(uid)
);

-- 创建购物车项表
CREATE TABLE wz_cart_items (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    cart_id INT,
    product_id INT,
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(10, 2) NOT NULL,
    product_name VARCHAR(255),  -- 商品名称
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES wz_carts(cart_id),
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id),
    UNIQUE (cart_id, product_id)  -- 确保同一商品在购物车中唯一
);

-- 创建商品图片表
CREATE TABLE wz_product_images (
    image_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT,
    image_url VARCHAR(255),
    is_primary TINYINT DEFAULT 0,  -- 是否为主图
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id)
); */
