USE store;

-- 先删除所有表（按照外键依赖的反序删除）
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS wz_order_items_archive;
DROP TABLE IF EXISTS wz_orders_archive;
DROP TABLE IF EXISTS wz_products_archive;
DROP TABLE IF EXISTS wz_users_archive;
DROP TABLE IF EXISTS wz_chat_messages_archive;
DROP TABLE IF EXISTS wz_chat_messages;
DROP TABLE IF EXISTS wz_chat_rooms;
DROP TABLE IF EXISTS wz_order_items;
DROP TABLE IF EXISTS wz_orders;
DROP TABLE IF EXISTS wz_cart_items;
DROP TABLE IF EXISTS wz_carts;
DROP TABLE IF EXISTS wz_browse_history;
DROP TABLE IF EXISTS wz_browser_fingerprints;
DROP TABLE IF EXISTS wz_product_images;
DROP TABLE IF EXISTS wz_products;
DROP TABLE IF EXISTS wz_categories;
DROP TABLE IF EXISTS wz_users;
DROP TABLE IF EXISTS wz_id_generator;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 创建ID生成器表（无外键依赖）
CREATE TABLE IF NOT EXISTS wz_id_generator (
    id_type VARCHAR(32) PRIMARY KEY,
    current_max_id BIGINT NOT NULL,
    step INT DEFAULT 100,
    version INT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 创建用户表（无外键依赖）
CREATE TABLE IF NOT EXISTS wz_users (
    uid VARCHAR(64) PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(256) NOT NULL,
    power VARCHAR(20) NOT NULL DEFAULT 'user',
    phone VARCHAR(20),
    email VARCHAR(50),
    gender INT,
    avatar VARCHAR(255),
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    created_user VARCHAR(20),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_user VARCHAR(20),
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 创建分类表（自引用外键）
CREATE TABLE IF NOT EXISTS wz_categories (
    category_id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id VARCHAR(64),
    level INT NOT NULL,
    sort_order INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',
    created_time DATETIME NOT NULL,
    modify_time DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 添加分类表的自引用外键（分开添加以避免创建表时的循环依赖）
ALTER TABLE wz_categories 
ADD CONSTRAINT fk_category_parent 
FOREIGN KEY (parent_id) REFERENCES wz_categories(category_id) ON DELETE SET NULL;

-- 4. 创建商品表（依赖分类表）
CREATE TABLE IF NOT EXISTS wz_products (
    product_id VARCHAR(64) PRIMARY KEY,
    category_id VARCHAR(64),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    brand VARCHAR(50),
    tags VARCHAR(255),
    rating DECIMAL(3,2),
    review_count INT,
    image_url VARCHAR(255),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_user VARCHAR(20),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_user VARCHAR(20),
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES wz_categories(category_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 创建商品图片表（依赖商品表）
CREATE TABLE IF NOT EXISTS wz_product_images (
    image_id VARCHAR(64) PRIMARY KEY,
    product_id VARCHAR(64),
    image_url VARCHAR(255),
    is_primary TINYINT DEFAULT 0,
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 创建浏览器指纹表（依赖用户表）
CREATE TABLE IF NOT EXISTS wz_browser_fingerprints (
    fingerprint_id VARCHAR(64) PRIMARY KEY,
    first_seen_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_seen_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id VARCHAR(64),
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 创建浏览记录表（依赖用户表、商品表、指纹表）
CREATE TABLE IF NOT EXISTS wz_browse_history (
    history_id VARCHAR(64) PRIMARY KEY,
    fingerprint_id VARCHAR(64),
    user_id VARCHAR(64),
    product_id VARCHAR(64),
    browse_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',
    FOREIGN KEY (fingerprint_id) REFERENCES wz_browser_fingerprints(fingerprint_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 创建购物车表（依赖用户表）
CREATE TABLE IF NOT EXISTS wz_carts (
    cart_id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    created_user VARCHAR(20),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_checked_out TINYINT(1) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. 创建购物车项表（依赖购物车表、商品表）
CREATE TABLE IF NOT EXISTS wz_cart_items (
    cart_item_id VARCHAR(64) PRIMARY KEY,
    cart_id VARCHAR(64) NOT NULL,
    product_id VARCHAR(64) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    created_user VARCHAR(20),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES wz_carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. 创建订单表（依赖用户表）
CREATE TABLE IF NOT EXISTS wz_orders (
    order_id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_id VARCHAR(32),
    created_user VARCHAR(20),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    expire_time DATETIME,
    pay_time DATETIME,
    version INT NOT NULL DEFAULT 1,
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    modified_user VARCHAR(20),
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES wz_users(uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. 创建订单项表（依赖订单表、商品表）
CREATE TABLE IF NOT EXISTS wz_order_items (
    order_item_id VARCHAR(64) PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL,
    product_id VARCHAR(64) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_user VARCHAR(20),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_user VARCHAR(20),
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES wz_orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建聊天室表
CREATE TABLE IF NOT EXISTS wz_chat_rooms (
    room_id VARCHAR(64) PRIMARY KEY,
    product_id VARCHAR(64) NOT NULL,
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建聊天记录表
CREATE TABLE IF NOT EXISTS wz_chat_messages (
    message_id VARCHAR(64) PRIMARY KEY,
    room_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    message TEXT NOT NULL,
    message_type ENUM('user', 'merchant') NOT NULL,
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES wz_chat_rooms(room_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES wz_users(uid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建聊天记录归档表
CREATE TABLE IF NOT EXISTS wz_chat_messages_archive (
    message_id VARCHAR(64) PRIMARY KEY,
    room_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    message TEXT NOT NULL,
    message_type ENUM('user', 'merchant') NOT NULL,
    created_user VARCHAR(50) NOT NULL DEFAULT 'system',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    archived_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建用户归档表
CREATE TABLE IF NOT EXISTS wz_users_archive (
    uid VARCHAR(64) PRIMARY KEY,
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
    product_id VARCHAR(64) PRIMARY KEY,   -- 改为VARCHAR
    category_id VARCHAR(64),              -- 改为VARCHAR
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
    order_id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64),
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
    order_item_id VARCHAR(64) PRIMARY KEY,  -- 改为VARCHAR
    order_id VARCHAR(64) NOT NULL,          -- 已经是VARCHAR
    product_id VARCHAR(64) NOT NULL,        -- 改为VARCHAR
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