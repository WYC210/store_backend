-- 添加浏览器指纹表
CREATE TABLE IF NOT EXISTS wz_browser_fingerprints (
    fingerprint_id VARCHAR(64) PRIMARY KEY,      -- 浏览器指纹ID
    first_seen_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 首次出现时间
    last_seen_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 最后出现时间
    user_id BIGINT,                             -- 关联的用户ID（登录后更新）
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE SET NULL
);

-- 添加浏览记录表
CREATE TABLE IF NOT EXISTS wz_browse_history (
    history_id BIGINT PRIMARY KEY,               -- 历史记录ID
    fingerprint_id VARCHAR(64),                  -- 浏览器指纹ID
    user_id BIGINT,                             -- 用户ID（可以为空，表示未登录）
    product_id BIGINT NOT NULL,                 -- 商品ID
    browse_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 浏览时间
    FOREIGN KEY (fingerprint_id) REFERENCES wz_browser_fingerprints(fingerprint_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE
);  -- 创建ID生成器表
CREATE TABLE wz_id_generator (
    id_type VARCHAR(32) PRIMARY KEY,            -- 业务类型，例如 'user', 'product', 'order' 等
    current_max_id BIGINT NOT NULL,             -- 当前最大ID
    step INT DEFAULT 100,                       -- 步长，用于批量生成ID
    version INT DEFAULT 1                        -- 版本号，用于乐观锁
);

-- 创建用户表
CREATE TABLE wz_users (
    uid BIGINT PRIMARY KEY,                     -- 用户ID，使用雪花算法生成
    username VARCHAR(20) NOT NULL UNIQUE,      -- 用户名，唯一
    password CHAR(60) NOT NULL,                 -- 密码，存储加密后的密码
    power VARCHAR(20),                          -- 用户级别或角色
    phone VARCHAR(15),                          -- 手机号
    email VARCHAR(30) UNIQUE,                   -- 邮箱，唯一
    gender TINYINT,                             -- 性别，0-未知，1-男，2-女
    avatar VARCHAR(50),                         -- 头像URL
    is_delete TINYINT DEFAULT 0,               -- 0表示未删除，1表示已删除
    created_user VARCHAR(20),                   -- 创建用户
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    modified_user VARCHAR(20),                  -- 修改用户
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  -- 修改时间
);

-- 创建商品分类表
CREATE TABLE wz_categories (
    category_id BIGINT PRIMARY KEY,             -- 分类ID，使用雪花算法生成
    name VARCHAR(255) NOT NULL UNIQUE,          -- 分类名称，唯一
    parent_category_id BIGINT,                  -- 父类别ID，用于多级分类
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 修改时间
    FOREIGN KEY (parent_category_id) REFERENCES wz_categories(category_id) ON DELETE SET NULL  -- 自引用外键
);

-- 创建商品表
CREATE TABLE wz_products (
    product_id BIGINT PRIMARY KEY,               -- 商品ID，使用雪花算法生成
    name VARCHAR(255) NOT NULL,                  -- 商品名称
    description TEXT,                            -- 商品描述
    price DECIMAL(10, 2) NOT NULL,               -- 商品价格
    stock INT NOT NULL,                          -- 商品库存
    category_id BIGINT,                          -- 分类ID，外键关联
    sku VARCHAR(50),                             -- 库存单位
    brand VARCHAR(100),                          -- 品牌
    tags VARCHAR(255),                           -- 以逗号分隔的标签
    rating DECIMAL(3, 2) DEFAULT 0,             -- 商品评分
    review_count INT DEFAULT 0,                  -- 评论数量
    low_stock_threshold INT DEFAULT 10,          -- 库存预警阈值
    is_active TINYINT DEFAULT 1,                -- 1表示上架，0表示下架
    image_url VARCHAR(255),                      -- 商品主图URL
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 修改时间
    created_by BIGINT,                           -- 创建该商品的用户ID
    FOREIGN KEY (category_id) REFERENCES wz_categories(category_id) ON DELETE SET NULL,  -- 关联分类表
    FOREIGN KEY (created_by) REFERENCES wz_users(uid) ON DELETE CASCADE  -- 关联用户表
);

-- 创建订单表
CREATE TABLE wz_orders (
    order_id BIGINT PRIMARY KEY,                 -- 订单ID，使用雪花算法生成
    user_id BIGINT,                             -- 用户ID，外键关联
    total_amount DECIMAL(10, 2) NOT NULL,       -- 总金额
    status ENUM('pending', 'completed', 'canceled') DEFAULT 'pending',  -- 订单状态
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
    FOREIGN KEY (order_id) REFERENCES wz_orders(order_id) ON DELETE CASCADE,  -- 关联订单表
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE  -- 关联商品表
);

-- 创建购物车表
CREATE TABLE wz_carts (
    cart_id BIGINT PRIMARY KEY,                  -- 购物车ID，使用雪花算法生成
    user_id BIGINT,                             -- 用户ID，外键关联
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
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE  -- 关联商品表
);

-- 创建聊天室表
CREATE TABLE wz_chat_rooms (
    room_id BIGINT PRIMARY KEY AUTO_INCREMENT,   -- 聊天室ID
    product_id BIGINT NOT NULL,                  -- 关联的商品ID
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
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    archived_time DATETIME DEFAULT CURRENT_TIMESTAMP  -- 归档时间
); 