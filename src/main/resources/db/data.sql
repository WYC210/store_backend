USE store;

-- 清空所有表数据（按照外键依赖的反序清空）
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE wz_order_items;
TRUNCATE TABLE wz_orders;
TRUNCATE TABLE wz_cart_items;
TRUNCATE TABLE wz_carts;
TRUNCATE TABLE wz_browse_history;
TRUNCATE TABLE wz_browser_fingerprints;
TRUNCATE TABLE wz_product_images;
TRUNCATE TABLE wz_products;
TRUNCATE TABLE wz_categories;
TRUNCATE TABLE wz_users;
TRUNCATE TABLE wz_id_generator;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. 初始化ID生成器
INSERT INTO wz_id_generator (id_type, current_max_id, step, version) VALUES
('user', 100, 100, 1),
('category', 100, 100, 1),
('product', 100, 100, 1),
('cart', 100, 100, 1),
('order', 100, 100, 1),
('cart_item', 100, 100, 1),
('order_item', 100, 100, 1);

-- 2. 初始化用户数据
INSERT INTO wz_users (uid, username, password, power, phone, email, gender, avatar, created_user, created_time) VALUES
('1', 'admin', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGM/TEZhPy7i', 'admin', '13800138000', 'admin@example.com', 1, 'admin.jpg', 'system', NOW()),
('2', 'test', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGM/TEZhPy7i', 'user', '13800138001', 'test@example.com', 0, 'default.jpg', 'system', NOW()),
('3', 'wz', '$2a$10$L9Ncd3EhHBeUBRyC4kN6t.ZMdWulVpwU38DF4R0n.q.ExZaSxLHTy', 'user', '6666666666', 'wz@example.com', 0, 'default.jpg', 'system', NOW());

-- 3. 初始化分类数据（使用事务确保数据一致性）
START TRANSACTION;

-- 先插入父分类
INSERT INTO wz_categories (category_id, name, parent_id, level, sort_order, created_user, created_time, modify_time) VALUES
('1', '电子产品', NULL, 1, 1, 'system', NOW(), NOW()),
('2', '服装', NULL, 1, 2, 'system', NOW(), NOW()),
('3', '食品', NULL, 1, 3, 'system', NOW(), NOW());

-- 然后插入子分类
INSERT INTO wz_categories (category_id, name, parent_id, level, sort_order, created_user, created_time, modify_time) VALUES
('11', '手机', '1', 2, 1, 'system', NOW(), NOW()),
('12', '电脑', '1', 2, 2, 'system', NOW(), NOW()),
('21', '男装', '2', 2, 1, 'system', NOW(), NOW()),
('22', '女装', '2', 2, 2, 'system', NOW(), NOW());

COMMIT;

-- 4. 初始化商品数据
INSERT INTO wz_products (product_id, name, description, price, stock, category_id, brand, tags, rating, review_count, image_url, is_active, created_user, created_time) VALUES
('1', 'iPhone 14', '最新款iPhone手机', 6999.00, 100, '11', 'Apple', 'phone,apple', 4.5, 100, '/images/iphone14.jpeg', 1, 'system', NOW()),
('2', 'MacBook Pro', '专业级笔记本电脑', 12999.00, 50, '12', 'Apple', 'laptop,apple', 4.8, 50, '/images/macbook.jpeg', 1, 'system', NOW()),
('3', 'Huawei P50', '华为旗舰手机', 5999.00, 80, '11', 'Huawei', 'phone,huawei', 4.6, 80, '/images/huawei.jpeg', 1, 'system', NOW());

-- 5. 初始化商品图片数据
INSERT INTO wz_product_images (image_id, product_id, image_url, is_primary, created_user) VALUES
('1', '1', '/images/iphone14.jpeg', 1, 'system'),
('2', '2', '/images/macbook.jpeg', 1, 'system'),
('3', '3', '/images/huawei.jpeg', 1, 'system');

-- 6. 初始化购物车数据
INSERT INTO wz_carts (cart_id, user_id, created_user, created_time) VALUES
('1', '1', 'system', NOW()),
('2', '2', 'system', NOW());

-- 7. 初始化购物车项数据
INSERT INTO wz_cart_items (cart_item_id, cart_id, product_id, quantity, price, product_name, created_user, created_time, paid_quantity) VALUES
('1', '1', '1', 1, 6999.00, 'iPhone 14', 'system', NOW(), 0),
('2', '1', '2', 1, 12999.00, 'MacBook Pro', 'system', NOW(), 0);

-- 8. 初始化订单数据
INSERT INTO wz_orders (order_id, user_id, total_amount, status, payment_id, created_user, created_time) VALUES
('1', '1', 19998.00, 'PAID', 'PAY123456', 'system', NOW()),
('2', '2', 6999.00, 'PENDING_PAY', NULL, 'system', NOW());

-- 9. 初始化订单项数据
INSERT INTO wz_order_items (
    order_item_id, 
    order_id, 
    product_id, 
    product_name, 
    quantity, 
    price, 
    created_user, 
    created_time,
    modified_user,
    modified_time
) VALUES
('1', '1', '1', 'iPhone 14', 1, 6999.00, 'system', NOW(), 'system', NOW()),
('2', '1', '2', 'MacBook Pro', 1, 12999.00, 'system', NOW(), 'system', NOW()),
('3', '2', '1', 'iPhone 14', 1, 6999.00, 'system', NOW(), 'system', NOW());