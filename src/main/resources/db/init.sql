use store
-- 初始化分类数据
INSERT INTO category (category_id, name, parent_category_id, created_user, created_time, modified_user, modified_time) VALUES
(1, '电子产品', NULL, 'admin', NOW(), 'admin', NOW()),
(2, '服装', NULL, 'admin', NOW(), 'admin', NOW()),
(3, '图书', NULL, 'admin', NOW(), 'admin', NOW()),
(11, '手机', 1, 'admin', NOW(), 'admin', NOW()),
(12, '电脑', 1, 'admin', NOW(), 'admin', NOW()),
(21, '男装', 2, 'admin', NOW(), 'admin', NOW()),
(22, '女装', 2, 'admin', NOW(), 'admin', NOW()),
(31, '小说', 3, 'admin', NOW(), 'admin', NOW()),
(32, '教育', 3, 'admin', NOW(), 'admin', NOW());

-- 初始化用户数据
INSERT INTO user (uid, username, password, power, phone, email, gender, avatar, is_delete, created_user, created_time, modified_user, modified_time) VALUES
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin', '13800138000', 'admin@example.com', 1, 'default.jpg', 0, 'system', NOW(), 'system', NOW()),
(2, 'test1', 'e10adc3949ba59abbe56e057f20f883e', 'user', '13800138001', 'test1@example.com', 1, 'default.jpg', 0, 'system', NOW(), 'system', NOW()),
(3, 'test2', 'e10adc3949ba59abbe56e057f20f883e', 'user', '13800138002', 'test2@example.com', 2, 'default.jpg', 0, 'system', NOW(), 'system', NOW());

-- 初始化商品数据
INSERT INTO product (product_id, name, description, price, stock, category_id, brand, tags, image_url, rating, review_count, is_active, created_user, created_time, modified_user, modified_time) VALUES
(1, 'iPhone 14', '苹果最新旗舰手机', 6999.00, 100, 11, 'Apple', '手机,苹果,5G', '/images/iphone14.jpg', 4.8, 120, true, 'admin', NOW(), 'admin', NOW()),
(2, 'MacBook Pro', '专业级笔记本电脑', 12999.00, 50, 12, 'Apple', '笔记本,苹果,电脑', '/images/macbook.jpg', 4.9, 80, true, 'admin', NOW(), 'admin', NOW()),
(3, '男士休闲夹克', '舒适百搭的夹克外套', 299.00, 200, 21, 'Brand A', '外套,夹克,男装', '/images/jacket.jpg', 4.5, 50, true, 'admin', NOW(), 'admin', NOW()),
(4, '连衣裙', '优雅时尚连衣裙', 199.00, 150, 22, 'Brand B', '裙子,女装,春装', '/images/dress.jpg', 4.7, 65, true, 'admin', NOW(), 'admin', NOW()),
(5, '三体全集', '刘慈欣科幻小说', 99.00, 300, 31, '重庆出版社', '科幻,小说,畅销书', '/images/santi.jpg', 4.9, 200, true, 'admin', NOW(), 'admin', NOW());

-- 初始化商品评论数据
INSERT INTO product_review (product_id, name, price, description, rating, review_count, created_time) VALUES
(1, 'iPhone 14', 6999.00, '手机很好用，拍照效果很棒！', 5.0, 1, NOW()),
(1, 'iPhone 14', 6999.00, '电池续航有待提高', 4.0, 1, NOW()),
(2, 'MacBook Pro', 12999.00, '性能强大，做视频剪辑很流畅', 5.0, 1, NOW()),
(3, '男士休闲夹克', 299.00, '面料很好，穿着舒服', 4.5, 1, NOW()),
(4, '连衣裙', 199.00, '款式很漂亮，但是尺码偏小', 4.0, 1, NOW());

-- 初始化购物车数据
INSERT INTO cart (cart_id, user_id, created_time, modified_time, is_checked_out) VALUES
(1, 2, NOW(), NOW(), false),
(2, 3, NOW(), NOW(), false);

-- 初始化购物车商品数据
INSERT INTO cart_item (cart_item_id, cart_id, product_id, quantity, price, product_name, created_time, modified_time) VALUES
(1, 1, 1, 1, 6999.00, 'iPhone 14', NOW(), NOW()),
(2, 1, 3, 2, 299.00, '男士休闲夹克', NOW(), NOW()),
(3, 2, 4, 1, 199.00, '连衣裙', NOW(), NOW());

-- ID生成器表
CREATE TABLE id_generator (
    id_type VARCHAR(32) PRIMARY KEY,
    current_max_id BIGINT NOT NULL,
    version INT NOT NULL DEFAULT 1
);

-- 用户表
CREATE TABLE user (
    uid BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    power VARCHAR(20) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    gender INT,
    avatar VARCHAR(255),
    is_delete INT DEFAULT 0,
    created_user VARCHAR(50),
    created_time DATETIME,
    modified_user VARCHAR(50),
    modified_time DATETIME
);

-- 分类表
CREATE TABLE category (
    category_id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_category_id BIGINT,
    created_user VARCHAR(50),
    created_time DATETIME,
    modified_user VARCHAR(50),
    modified_time DATETIME
);

-- 商品表
CREATE TABLE product (
    product_id BIGINT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    category_id BIGINT,
    brand VARCHAR(100),
    tags VARCHAR(255),
    image_url VARCHAR(255),
    rating DECIMAL(2,1),
    review_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_user VARCHAR(50),
    created_time DATETIME,
    modified_user VARCHAR(50),
    modified_time DATETIME
);

-- 商品评论表
CREATE TABLE product_review (
    review_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT,
    name VARCHAR(200),
    price DECIMAL(10,2),
    description TEXT,
    rating DECIMAL(2,1),
    review_count INT,
    created_time DATETIME
);

-- 购物车表
CREATE TABLE cart (
    cart_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_time DATETIME,
    modified_time DATETIME,
    is_checked_out BOOLEAN DEFAULT FALSE
);

-- 购物车商品表
CREATE TABLE cart_item (
    cart_item_id BIGINT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    product_name VARCHAR(200),
    created_time DATETIME,
    modified_time DATETIME
);

-- 浏览历史表
CREATE TABLE browse_history (
    history_id BIGINT PRIMARY KEY,
    fingerprint_id VARCHAR(100),
    user_id BIGINT,
    product_id BIGINT,
    browse_time DATETIME
);

-- 浏览器指纹表
CREATE TABLE browser_fingerprint (
    fingerprint_id VARCHAR(100) PRIMARY KEY,
    first_seen_time DATETIME,
    last_seen_time DATETIME,
    user_id BIGINT
); 