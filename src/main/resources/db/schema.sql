USE store;

-- 创建ID生成器表
CREATE TABLE IF NOT EXISTS wz_id_generator (
    id_type VARCHAR(32) PRIMARY KEY,            -- 业务类型，例如 'user', 'product', 'order' 等
    current_max_id BIGINT NOT NULL,             -- 当前最大ID
    step INT DEFAULT 100,                       -- 步长，用于批量生成ID
    version INT DEFAULT 1                        -- 版本号，用于乐观锁
);

-- 创建用户表
CREATE TABLE IF NOT EXISTS wz_users (
    uid BIGINT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(256) NOT NULL,
    power VARCHAR(20) NOT NULL DEFAULT 'user',  -- 用户权限：admin/user等
    phone VARCHAR(20),
    email VARCHAR(50),
    gender INT,
    avatar VARCHAR(255),                        -- 头像URL
    is_delete TINYINT(1) NOT NULL DEFAULT 0,    -- 0未删除，1已删除
    created_user VARCHAR(20),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_user VARCHAR(20),
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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
    category_id BIGINT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    brand VARCHAR(50),
    tags VARCHAR(255),
    rating DECIMAL(3,2),
    review_count INT,
    image_url VARCHAR(255),
    is_active TINYINT(1) NOT NULL DEFAULT 1,    -- 1-上架，0-下架
    created_user VARCHAR(20),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_user VARCHAR(20),
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES wz_categories(category_id)
);

-- 创建商品图片表
CREATE TABLE IF NOT EXISTS wz_product_images (
    image_id BIGINT PRIMARY KEY,
    product_id BIGINT,
    image_url VARCHAR(255),
    is_primary TINYINT DEFAULT 0,               -- 是否为主图
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE
);

-- 创建浏览器指纹表
CREATE TABLE IF NOT EXISTS wz_browser_fingerprints (
    fingerprint_id VARCHAR(64) PRIMARY KEY,     -- 浏览器指纹ID
    first_seen_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 首次出现时间
    last_seen_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 最后出现时间
    user_id BIGINT,                             -- 关联的用户ID（登录后更新）
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE SET NULL
);

-- 创建浏览记录表
CREATE TABLE IF NOT EXISTS wz_browse_history (
    history_id BIGINT PRIMARY KEY,              -- 历史记录ID
    fingerprint_id VARCHAR(64),                 -- 浏览器指纹ID
    user_id BIGINT,                             -- 用户ID（可以为空，表示未登录）
    product_id BIGINT NOT NULL,                 -- 商品ID
    browse_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 浏览时间
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    FOREIGN KEY (fingerprint_id) REFERENCES wz_browser_fingerprints(fingerprint_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE
);

-- 创建购物车表
CREATE TABLE IF NOT EXISTS wz_carts (
    cart_id VARCHAR(64) PRIMARY KEY,
    user_id BIGINT,                    -- 改回 BIGINT，因为 user 表的 uid 是 BIGINT
    created_user VARCHAR(20),
    created_time DATETIME,
    modified_time DATETIME,
    is_checked_out TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES wz_users(uid)
);

-- 创建购物车项表
CREATE TABLE IF NOT EXISTS wz_cart_items (
    cart_item_id VARCHAR(64) PRIMARY KEY,
    cart_id VARCHAR(64),
    product_id BIGINT,                 -- 改回 BIGINT，因为 product 表的 id 是 BIGINT
    quantity INT,
    price DECIMAL(10,2),
    product_name VARCHAR(100),
    created_user VARCHAR(20),
    created_time DATETIME,
    modified_time DATETIME,
    FOREIGN KEY (cart_id) REFERENCES wz_carts(cart_id),
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id)
);

-- 创建订单表
CREATE TABLE IF NOT EXISTS wz_orders (
    order_id VARCHAR(32) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_time DATETIME NOT NULL,
    expire_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pay_time DATETIME,
    payment_id VARCHAR(32),
    version INT NOT NULL DEFAULT 1,
    created_user VARCHAR(20),
    modified_user VARCHAR(20),
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE CASCADE
);

-- 创建订单项表
CREATE TABLE IF NOT EXISTS wz_order_items (
    order_item_id BIGINT PRIMARY KEY,
    order_id VARCHAR(32) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL DEFAULT '',  -- 添加默认值
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_user VARCHAR(20),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_user VARCHAR(20),
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES wz_orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE
);

-- 创建聊天室表
CREATE TABLE IF NOT EXISTS wz_chat_rooms (
    room_id BIGINT PRIMARY KEY AUTO_INCREMENT,  -- 聊天室ID
    product_id BIGINT NOT NULL,                 -- 关联的商品ID
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 修改时间
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE
);

-- 创建聊天记录表
CREATE TABLE IF NOT EXISTS wz_chat_messages (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,  -- 消息ID
    room_id BIGINT NOT NULL,                       -- 聊天室ID，外键关联
    user_id BIGINT NOT NULL,                       -- 发送消息的用户ID
    message TEXT NOT NULL,                         -- 消息内容
    message_type ENUM('user', 'merchant') NOT NULL, -- 消息类型：用户或商家
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    FOREIGN KEY (room_id) REFERENCES wz_chat_rooms(room_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE CASCADE
);

-- 创建聊天记录归档表
CREATE TABLE IF NOT EXISTS wz_chat_messages_archive (
    message_id BIGINT PRIMARY KEY,               -- 消息ID
    room_id BIGINT NOT NULL,                     -- 聊天室ID
    user_id BIGINT NOT NULL,                     -- 发送消息的用户ID
    message TEXT NOT NULL,                       -- 消息内容
    message_type ENUM('user', 'merchant') NOT NULL, -- 消息类型
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',  -- 创建者
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    archived_time DATETIME DEFAULT CURRENT_TIMESTAMP  -- 归档时间
);

-- 创建用户归档表
CREATE TABLE IF NOT EXISTS wz_users_archive (
    uid BIGINT PRIMARY KEY,
    username VARCHAR(20) NOT NULL,
    power VARCHAR(20) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(50),
    gender INT,
    avatar VARCHAR(255),
    is_delete TINYINT(1) NOT NULL,
    created_user VARCHAR(20),
    created_time DATETIME,
    modified_user VARCHAR(20),
    modified_time DATETIME,
    archived_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 创建商品归档表
CREATE TABLE IF NOT EXISTS wz_products_archive (
    product_id BIGINT PRIMARY KEY,
    category_id BIGINT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    brand VARCHAR(50),
    tags VARCHAR(255),
    rating DECIMAL(3,2),
    review_count INT,
    image_url VARCHAR(255),
    is_active TINYINT(1) NOT NULL,
    created_user VARCHAR(20),
    created_time DATETIME,
    modified_user VARCHAR(20),
    modified_time DATETIME,
    archived_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES wz_categories(category_id)
);

-- 创建订单归档表
CREATE TABLE IF NOT EXISTS wz_orders_archive (
    order_id VARCHAR(32) PRIMARY KEY,
    user_id BIGINT,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_time DATETIME,
    expire_time DATETIME,
    pay_time DATETIME,
    payment_id VARCHAR(32),
    version INT,
    is_delete TINYINT(1) NOT NULL,
    created_user VARCHAR(20),
    modified_user VARCHAR(20),
    modified_time DATETIME,
    archived_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES wz_users(uid)
);

-- 创建订单项归档表
CREATE TABLE IF NOT EXISTS wz_order_items_archive (
    order_item_id BIGINT PRIMARY KEY,
    order_id VARCHAR(32) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_user VARCHAR(20),
    created_time DATETIME,
    modified_user VARCHAR(20),
    modified_time DATETIME,
    archived_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES wz_orders_archive(order_id),
    FOREIGN KEY (product_id) REFERENCES wz_products_archive(product_id)
);

-- 如果需要为已存在的表添加字段，使用单独的语句
-- ALTER TABLE wz_orders ADD COLUMN IF NOT EXISTS is_delete TINYINT(1) NOT NULL DEFAULT 0;