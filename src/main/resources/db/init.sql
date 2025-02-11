USE store;

-- 添加浏览器指纹表
CREATE TABLE IF NOT EXISTS wz_browser_fingerprints (
    fingerprint_id VARCHAR(64) PRIMARY KEY,      -- 浏览器指纹ID
    first_seen_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 首次出现时间
    last_seen_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 最后出现时间
    user_id BIGINT,                             -- 关联的用户ID（登录后更新）
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE SET NULL
);

-- 添加浏览记录表
CREATE TABLE IF NOT EXISTS wz_browse_history (
    history_id BIGINT PRIMARY KEY,               -- 历史记录ID
    fingerprint_id VARCHAR(64),                  -- 浏览器指纹ID
    user_id BIGINT,                             -- 用户ID（可以为空，表示未登录）
    product_id BIGINT NOT NULL,                 -- 商品ID
    browse_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 浏览时间
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    FOREIGN KEY (fingerprint_id) REFERENCES wz_browser_fingerprints(fingerprint_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE
);

-- 创建ID生成器表
CREATE TABLE wz_id_generator (
    id_type VARCHAR(32) PRIMARY KEY,            -- 业务类型，例如 'user', 'product', 'order' 等
    current_max_id BIGINT NOT NULL,             -- 当前最大ID
    step INT DEFAULT 100,                       -- 步长，用于批量生成ID
    version INT DEFAULT 1                        -- 版本号，用于乐观锁
);

-- 创建分类表
CREATE TABLE IF NOT EXISTS wz_categories (
    category_id BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT,
    level INT NOT NULL,
    sort_order INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    created_time DATETIME NOT NULL,
    modify_time DATETIME NOT NULL,
    FOREIGN KEY (parent_id) REFERENCES wz_categories(category_id)
);

-- 创建商品表
CREATE TABLE IF NOT EXISTS wz_products (
    product_id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    category_id BIGINT NOT NULL,
    brand VARCHAR(50),
    tags VARCHAR(200),
    rating DECIMAL(2,1),
    review_count INT DEFAULT 0,
    image_url VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    created_time DATETIME NOT NULL,
    modify_time DATETIME NOT NULL,
    FOREIGN KEY (category_id) REFERENCES wz_categories(category_id)
);

-- 创建用户表
CREATE TABLE IF NOT EXISTS wz_users (
    uid BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,  -- 修改为 100 以适应加密后的密码长度
    power VARCHAR(20) NOT NULL DEFAULT 'user',  -- 用户权限：admin/user
    phone VARCHAR(20),                          -- 添加电话字段
    email VARCHAR(100),                         -- 添加邮箱字段
    gender TINYINT,                            -- 添加性别字段：0-女，1-男
    avatar VARCHAR(255),                        -- 添加头像URL字段
    is_delete BOOLEAN DEFAULT FALSE,            -- 是否删除
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_user VARCHAR(50),
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
);

-- 创建订单表
CREATE TABLE wz_orders (
    order_id BIGINT PRIMARY KEY,                 -- 订单ID，使用雪花算法生成
    user_id BIGINT,                             -- 用户ID，外键关联
    total_amount DECIMAL(10, 2) NOT NULL,       -- 总金额
    status ENUM('pending', 'completed', 'canceled') DEFAULT 'pending',  -- 订单状态
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 修改时间
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE CASCADE  -- 关联用户表
);

-- 创建订单项表
CREATE TABLE wz_order_items (
    order_item_id BIGINT PRIMARY KEY,            -- 订单项ID，使用雪花算法生成
    order_id BIGINT,                             -- 订单ID，外键关联
    product_id BIGINT,                           -- 商品ID，外键关联
    quantity INT NOT NULL,                       -- 商品数量
    price DECIMAL(10, 2) NOT NULL,               -- 商品价格
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    FOREIGN KEY (order_id) REFERENCES wz_orders(order_id) ON DELETE CASCADE,  -- 关联订单表
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE  -- 关联商品表
);

-- 创建购物车表
CREATE TABLE wz_carts (
    cart_id BIGINT PRIMARY KEY,                  -- 购物车ID，使用雪花算法生成
    user_id BIGINT,                             -- 用户ID，外键关联
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 修改时间
    is_checked_out TINYINT DEFAULT 0,           -- 0表示未结算，1表示已结算
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE CASCADE  -- 关联用户表
);

-- 创建购物车项表
CREATE TABLE wz_cart_items (
    cart_item_id BIGINT PRIMARY KEY,             -- 购物车项ID，使用雪花算法生成
    cart_id BIGINT,                              -- 购物车ID，外键关联
    product_id BIGINT,                           -- 商品ID，外键关联
    quantity INT NOT NULL DEFAULT 1,             -- 商品数量
    price DECIMAL(10, 2) NOT NULL,               -- 商品价格
    product_name VARCHAR(255),                   -- 商品名称
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 修改时间
    FOREIGN KEY (cart_id) REFERENCES wz_carts(cart_id) ON DELETE CASCADE,  -- 关联购物车表
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE,  -- 关联商品表
    UNIQUE (cart_id, product_id)                 -- 确保同一商品在购物车中唯一
);

-- 创建商品图片表
CREATE TABLE wz_product_images (
    image_id BIGINT PRIMARY KEY,                 -- 图片ID，使用雪花算法生成
    product_id BIGINT,                           -- 商品ID，外键关联
    image_url VARCHAR(255),                      -- 图片URL
    is_primary TINYINT DEFAULT 0,                -- 是否为主图
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE  -- 关联商品表
);

-- 创建聊天室表
CREATE TABLE wz_chat_rooms (
    room_id BIGINT PRIMARY KEY AUTO_INCREMENT,   -- 聊天室ID
    product_id BIGINT NOT NULL,                  -- 关联的商品ID
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 修改时间
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE  -- 关联商品表
);

-- 创建聊天记录表
CREATE TABLE wz_chat_messages (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,  -- 消息ID
    room_id BIGINT NOT NULL,                       -- 聊天室ID，外键关联
    user_id BIGINT NOT NULL,                       -- 发送消息的用户ID
    message TEXT NOT NULL,                         -- 消息内容
    message_type ENUM('user', 'merchant') NOT NULL, -- 消息类型：用户或商家
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    FOREIGN KEY (room_id) REFERENCES wz_chat_rooms(room_id) ON DELETE CASCADE,  -- 关联聊天室表
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE CASCADE  -- 关联用户表
);

-- 创建聊天记录归档表
CREATE TABLE wz_chat_messages_archive (
    message_id BIGINT PRIMARY KEY,               -- 消息ID
    room_id BIGINT NOT NULL,                     -- 聊天室ID
    user_id BIGINT NOT NULL,                     -- 发送消息的用户ID
    message TEXT NOT NULL,                       -- 消息内容
    message_type ENUM('user', 'merchant') NOT NULL, -- 消息类型
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    archived_time DATETIME DEFAULT CURRENT_TIMESTAMP  -- 归档时间
);

-- 在表结构定义之后添加以下初始化数据

-- 初始化ID生成器数据
INSERT INTO wz_id_generator (id_type, current_max_id, step, version) VALUES
('user', 1000, 100, 1),
('category', 1000, 100, 1),
('product', 1000, 100, 1),
('cart', 1000, 100, 1),
('order', 1000, 100, 1);

-- 初始化用户数据
INSERT INTO wz_users (
    uid, username, password, power, phone, email, gender, avatar, 
    is_delete, created_user, created_time, modified_user, modified_time
) VALUES 
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin', '13800138000', 'admin@example.com', 1, '/images/avatar/default.png',
    false, 'system', NOW(), 'system', NOW()),
(2, 'test1', 'e10adc3949ba59abbe56e057f20f883e', 'user', '13800138001', 'test1@example.com', 1, '/images/avatar/default.png',
    false, 'system', NOW(), 'system', NOW()),
(3, 'test2', 'e10adc3949ba59abbe56e057f20f883e', 'user', '13800138002', 'test2@example.com', 0, '/images/avatar/default.png',
    false, 'system', NOW(), 'system', NOW());

-- 初始化分类数据
INSERT INTO wz_categories (category_id, name, parent_id, level, sort_order, is_active, created_user, created_time, modify_time) VALUES
-- 父分类
(1, '电子产品', NULL, 1, 1, true, 'system', NOW(), NOW()),
(2, '服装', NULL, 1, 2, true, 'system', NOW(), NOW()),
(3, '图书', NULL, 1, 3, true, 'system', NOW(), NOW()),
-- 子分类
(11, '手机', 1, 2, 1, true, 'system', NOW(), NOW()),
(12, '电脑', 1, 2, 2, true, 'system', NOW(), NOW()),
(21, '男装', 2, 2, 1, true, 'system', NOW(), NOW()),
(22, '女装', 2, 2, 2, true, 'system', NOW(), NOW()),
(31, '小说', 3, 2, 1, true, 'system', NOW(), NOW()),
(32, '教育', 3, 2, 2, true, 'system', NOW(), NOW());

-- 初始化商品数据
INSERT INTO wz_products (
    product_id, name, description, price, stock, category_id, 
    brand, tags, rating, review_count, image_url, is_active,
    created_user, created_time, modify_time
) VALUES
(1, 'iPhone 14', '苹果最新旗舰手机', 6999.00, 100, 11, 'Apple', '手机,苹果,5G', 4.8, 120, '/images/products/iphone14.jpg', true, 'system', NOW(), NOW()),
(2, 'MacBook Pro', '专业级笔记本电脑', 12999.00, 50, 12, 'Apple', '笔记本,苹果,电脑', 4.9, 80, '/images/products/macbook.jpg', true, 'system', NOW(), NOW()),
(3, '男士休闲夹克', '舒适百搭的夹克外套', 299.00, 200, 21, 'Brand A', '外套,夹克,男装', 4.5, 50, '/images/products/jacket.jpg', true, 'system', NOW(), NOW()),
(4, '连衣裙', '优雅时尚连衣裙', 199.00, 150, 22, 'Brand B', '裙子,女装,春装', 4.7, 65, '/images/products/dress.jpg', true, 'system', NOW(), NOW()),
(5, '三体全集', '刘慈欣科幻小说', 99.00, 300, 31, '重庆出版社', '科幻,小说,畅销书', 4.9, 200, '/images/products/santi.jpg', true, 'system', NOW(), NOW());

-- 初始化购物车数据
INSERT INTO wz_carts (cart_id, user_id, created_time, modified_time, is_checked_out) VALUES
(1, 2, NOW(), NOW(), 0),
(2, 3, NOW(), NOW(), 0);

-- 初始化购物车商品数据
INSERT INTO wz_cart_items (cart_item_id, cart_id, product_id, quantity, price, product_name, created_time, modified_time) VALUES
(1, 1, 1, 1, 6999.00, 'iPhone 14', NOW(), NOW()),
(2, 1, 3, 2, 299.00, '男士休闲夹克', NOW(), NOW()),
(3, 2, 4, 1, 199.00, '连衣裙', NOW(), NOW()); 

-- 初始化浏览器指纹数据
INSERT INTO wz_browser_fingerprints (fingerprint_id, first_seen_time, last_seen_time, user_id) VALUES
('fp_001', NOW(), NOW(), 2),
('fp_002', NOW(), NOW(), 3),
('fp_003', NOW(), NOW(), NULL);

-- 初始化浏览历史数据
INSERT INTO wz_browse_history (history_id, fingerprint_id, user_id, product_id, browse_time) VALUES
(1, 'fp_001', 2, 1, NOW()),
(2, 'fp_001', 2, 2, NOW()),
(3, 'fp_002', 3, 3, NOW()),
(4, 'fp_003', NULL, 4, NOW()); 