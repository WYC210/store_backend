/* 
-- 使用数据库
USE store;
-- 删除聊天记录表
DROP TABLE IF EXISTS chat_messages;

-- 删除聊天室表
DROP TABLE IF EXISTS chat_rooms;

-- 删除购物车项表
DROP TABLE IF EXISTS wz_cart_items;

-- 删除购物车表
DROP TABLE IF EXISTS wz_carts;

-- 删除订单项表
DROP TABLE IF EXISTS wz_order_items;

-- 删除订单表
DROP TABLE IF EXISTS wz_orders;

-- 删除商品图片表
DROP TABLE IF EXISTS wz_product_images;

-- 删除商品表
DROP TABLE IF EXISTS wz_products;

-- 删除商品分类表
DROP TABLE IF EXISTS wz_categories;

-- 删除用户表
DROP TABLE IF EXISTS wz_users;

-- 删除ID生成器表
DROP TABLE IF EXISTS t_id_generator;

DROP TABLE IF EXISTS chat_messages_archive;
*/
/* -- 创建ID生成器表
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
use store;
-- 插入商品分类数据
INSERT INTO wz_categories (category_id, name, parent_category_id) VALUES
(1, '手机数码', null),
(2, '智能手机', 1),
(3, '平板电脑', 1),
(4, '电脑办公', null),
(5, '笔记本', 4),
(6, '台式机', 4);

-- 插入测试用户（假设这是管理员账号）
-- 插入管理员账号（密码：admin123）
INSERT INTO wz_users (
    uid, username, password, power, phone, email, 
    gender, avatar, is_delete, created_user, modified_user
) VALUES (
    1, 
    'admin',
    '$2a$10$6Uc8ptpgTKKg.wnxY7SyguFAVkdv2qkCXeJE3UeYF9V5./tJFg3xu',  -- 加密后的 'admin123'
    'admin',
    '13800138000',
    'admin@example.com',
    1,
    'default.jpg',
    0,
    'system',
    'system'
);
-- 插入商品数据
INSERT INTO wz_products (
    product_id, name, description, price, stock, 
    category_id, sku, brand, tags, 
    rating, review_count, is_active, image_url, created_by
) VALUES
(1, 'iPhone 15 Pro', '苹果最新旗舰手机，搭载 A17 Pro 芯片', 8999.00, 100, 
2, 'IPHONE15PRO-256G', 'Apple', 'iPhone,苹果,手机', 
4.9, 120, 1, 'iphone15pro.jpg', 1),

(2, 'MacBook Pro 14', 'M3 Pro芯片，14英寸视网膜显示屏', 14999.00, 50, 
5, 'MACBOOK-M3-14', 'Apple', 'MacBook,笔记本,苹果', 
4.8, 85, 1, 'macbook14.jpg', 1),

(3, '华为 Mate 60 Pro', '麒麟芯片，卫星通讯', 6999.00, 80, 
2, 'MATE60PRO-256G', 'HUAWEI', '华为,手机,5G', 
4.7, 200, 1, 'mate60pro.jpg', 1),

(4, 'iPad Pro 2023', 'M2芯片，12.9英寸液态视网膜XDR显示屏', 7299.00, 60, 
3, 'IPADPRO-256G', 'Apple', 'iPad,平板,苹果', 
4.8, 150, 1, 'ipadpro.jpg', 1),

(5, '联想 小新Pro16', 'AMD R7处理器，16英寸2.5K屏', 5999.00, 70, 
5, 'XIAOXIN-PRO16', 'Lenovo', '联想,笔记本,AMD', 
4.6, 180, 1, 'xiaoxinpro16.jpg', 1);

-- 插入商品图片
INSERT INTO wz_product_images (image_id, product_id, image_url, is_primary) VALUES
(1, 1, 'iphone15pro_1.jpg', 1),
(2, 1, 'iphone15pro_2.jpg', 0),
(3, 2, 'macbook14_1.jpg', 1),
(4, 2, 'macbook14_2.jpg', 0),
(5, 3, 'mate60pro_1.jpg', 1),
(6, 3, 'mate60pro_2.jpg', 0),
(7, 4, 'ipadpro_1.jpg', 1),
(8, 4, 'ipadpro_2.jpg', 0),
(9, 5, 'xiaoxinpro16_1.jpg', 1),
(10, 5, 'xiaoxinpro16_2.jpg', 0); */
use store;
-- 创建商品评论表
CREATE TABLE wz_product_reviews (
    review_id BIGINT PRIMARY KEY,             -- 评论ID
    product_id BIGINT NOT NULL,               -- 商品ID
    user_id BIGINT NOT NULL,                  -- 用户ID
    rating DECIMAL(2,1) NOT NULL,             -- 评分(1-5分)
    content TEXT NOT NULL,                    -- 评论内容
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    FOREIGN KEY (product_id) REFERENCES wz_products(product_id),
    FOREIGN KEY (user_id) REFERENCES wz_users(uid)
);

-- 插入评论数据
INSERT INTO wz_product_reviews (review_id, product_id, user_id, rating, content, created_time) VALUES
-- iPhone 15 Pro 的评论
(1, 1, 1, 5.0, '这款手机真的太棒了！A17 Pro芯片性能强劲，拍照效果极佳，尤其是在弱光环境下表现出色。', '2024-01-15 10:30:00'),
(2, 1, 1, 4.5, '外观设计非常精致，钛金属边框手感极佳，就是价格有点小贵。', '2024-01-16 14:20:00'),
(3, 1, 1, 5.0, '相机系统升级明显，微距拍摄很惊艳，电池续航也不错。', '2024-01-17 09:15:00'),
(4, 1, 1, 4.0, '整体很满意，就是充电速度可以再快一点。', '2024-01-18 16:40:00'),

-- MacBook Pro 14 的评论
(5, 2, 1, 5.0, 'M3 Pro芯片性能强大，剪辑视频快得飞起，发热控制得也很好。', '2024-01-15 11:20:00'),
(6, 2, 1, 4.5, '屏幕素质一流，色准非常高，对设计工作很有帮助。', '2024-01-16 13:30:00'),
(7, 2, 1, 5.0, '键盘手感极佳，触控板依然是业界标杆，系统流畅度满分。', '2024-01-17 15:45:00'),
(8, 2, 1, 4.5, '接口丰富，终于不用带转接器了，就是价格确实不便宜。', '2024-01-18 10:25:00'),

-- 华为 Mate 60 Pro 的评论
(9, 3, 1, 5.0, '卫星通讯功能很实用，在野外也能保持联系，拍照水平一流。', '2024-01-15 09:40:00'),
(10, 3, 1, 4.5, '麒麟芯片性能不错，系统优化得很好，续航能力强。', '2024-01-16 16:50:00'),
(11, 3, 1, 5.0, '外观设计很有质感，曲面屏显示效果出色。', '2024-01-17 11:35:00'),
(12, 3, 1, 4.5, '相机系统全面，变焦性能优秀，就是重量稍微有点大。', '2024-01-18 14:15:00'),

-- iPad Pro 2023 的评论
(13, 4, 1, 5.0, 'M2芯片性能强劲，搭配妙控键盘几乎可以完全替代笔记本。', '2024-01-15 13:20:00'),
(14, 4, 1, 4.5, '屏幕素质顶级，用来看视频和画画都很享受。', '2024-01-16 10:40:00'),
(15, 4, 1, 5.0, '配合 Apple Pencil 使用体验极佳，延迟感几乎没有。', '2024-01-17 15:30:00'),
(16, 4, 1, 4.5, '系统流畅，多任务处理能力强，就是配件价格偏高。', '2024-01-18 11:50:00'),

-- 联想小新 Pro16 的评论
(17, 5, 1, 4.5, 'AMD处理器性能强劲，多开程序也不卡顿，性价比很高。', '2024-01-15 14:25:00'),
(18, 5, 1, 4.0, '2.5K屏幕显示效果不错，色彩表现准确，适合日常办公。', '2024-01-16 09:30:00'),
(19, 5, 1, 4.5, '散热系统设计合理，长时间使用也不会太烫。', '2024-01-17 16:40:00'),
(20, 5, 1, 4.0, '键盘手感舒适，触控板灵敏度适中，整体很均衡。', '2024-01-18 13:15:00');

-- 更新商品表的评分和评论数
UPDATE wz_products SET 
    rating = 4.9,
    review_count = 4
WHERE product_id = 1;

UPDATE wz_products SET 
    rating = 4.8,
    review_count = 4
WHERE product_id = 2;

UPDATE wz_products SET 
    rating = 4.7,
    review_count = 4
WHERE product_id = 3;

UPDATE wz_products SET 
    rating = 4.8,
    review_count = 4
WHERE product_id = 4;

UPDATE wz_products SET 
    rating = 4.6,
    review_count = 4
WHERE product_id = 5;