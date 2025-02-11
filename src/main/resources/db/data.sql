USE store;

-- 清空所有表数据（按照外键依赖的反序清空）
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE wz_chat_messages_archive;
TRUNCATE TABLE wz_chat_messages;
TRUNCATE TABLE wz_chat_rooms;
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
('user', 1000, 100, 1),
('category', 1000, 100, 1),
('product', 1000, 100, 1),
('cart', 1000, 100, 1),
('order', 1000, 100, 1);

-- 2. 初始化用户数据
INSERT INTO wz_users (uid, username, password, power, phone, email, gender, avatar, is_delete, created_user, created_time, modified_user, modified_time) VALUES
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin', '13800138000', 'admin@example.com', 1, '/images/avatar/default.png', 0, 'system', NOW(), 'system', NOW()),
(2, 'test1', 'e10adc3949ba59abbe56e057f20f883e', 'user', '13800138001', 'test1@example.com', 1, '/images/avatar/default.png', 0, 'system', NOW(), 'system', NOW()),
(3, 'test2', 'e10adc3949ba59abbe56e057f20f883e', 'user', '13800138002', 'test2@example.com', 0, '/images/avatar/default.png', 0, 'system', NOW(), 'system', NOW());

-- 3. 初始化分类数据
INSERT INTO wz_categories (category_id, name, parent_id, level, sort_order, is_active, created_user, created_time, modify_time) VALUES
(1, '电子产品', NULL, 1, 1, true, 'system', NOW(), NOW()),
(2, '服装', NULL, 1, 2, true, 'system', NOW(), NOW()),
(3, '图书', NULL, 1, 3, true, 'system', NOW(), NOW()),
(11, '手机', 1, 2, 1, true, 'system', NOW(), NOW()),
(12, '电脑', 1, 2, 2, true, 'system', NOW(), NOW()),
(21, '男装', 2, 2, 1, true, 'system', NOW(), NOW()),
(22, '女装', 2, 2, 2, true, 'system', NOW(), NOW()),
(31, '小说', 3, 2, 1, true, 'system', NOW(), NOW()),
(32, '教育', 3, 2, 2, true, 'system', NOW(), NOW());

-- 4. 初始化商品数据（字段顺序已修正）
INSERT INTO wz_products (product_id, category_id, name, description, price, stock, brand, tags, rating, review_count, image_url, is_active, created_user, created_time, modified_user, modified_time) VALUES
(1, 11, 'iPhone 14', '苹果最新旗舰手机', 6999.00, 100, 'Apple', '手机,苹果,5G', 4.8, 120, '/images/products/iphone14.jpg', 1, 'system', NOW(), 'system', NOW()),
(2, 12, 'MacBook Pro', '专业级笔记本电脑', 12999.00, 50, 'Apple', '笔记本,苹果,电脑', 4.9, 80, '/images/products/macbook.jpg', 1, 'system', NOW(), 'system', NOW()),
(3, 21, '男士休闲夹克', '舒适百搭的夹克外套', 299.00, 200, 'Brand A', '外套,夹克,男装', 4.5, 50, '/images/products/jacket.jpg', 1, 'system', NOW(), 'system', NOW()),
(4, 22, '连衣裙', '优雅时尚连衣裙', 199.00, 150, 'Brand B', '裙子,女装,春装', 4.7, 65, '/images/products/dress.jpg', 1, 'system', NOW(), 'system', NOW()),
(5, 31, '三体全集', '刘慈欣科幻小说', 99.00, 300, '重庆出版社', '科幻,小说,畅销书', 4.9, 200, '/images/products/santi.jpg', 1, 'system', NOW(), 'system', NOW());

-- 5. 初始化商品图片数据
INSERT INTO wz_product_images (image_id, product_id, image_url, is_primary) VALUES
(1, 1, '/images/products/iphone14_1.jpg', 1),
(2, 1, '/images/products/iphone14_2.jpg', 0),
(3, 2, '/images/products/macbook_1.jpg', 1),
(4, 3, '/images/products/jacket_1.jpg', 1),
(5, 4, '/images/products/dress_1.jpg', 1);

-- 6. 初始化浏览器指纹数据
INSERT INTO wz_browser_fingerprints (fingerprint_id, first_seen_time, last_seen_time, user_id) VALUES
('fp_001', NOW(), NOW(), 2),
('fp_002', NOW(), NOW(), 3),
('fp_003', NOW(), NOW(), NULL);

-- 7. 初始化浏览历史数据
INSERT INTO wz_browse_history (history_id, fingerprint_id, user_id, product_id, browse_time) VALUES
(1, 'fp_001', 2, 1, NOW()),
(2, 'fp_001', 2, 2, NOW()),
(3, 'fp_002', 3, 3, NOW()),
(4, 'fp_003', NULL, 4, NOW());

-- 8. 初始化购物车数据
INSERT INTO wz_carts (cart_id, user_id, created_time, modified_time, is_checked_out) VALUES
(1, 2, NOW(), NOW(), 0),
(2, 3, NOW(), NOW(), 0);

-- 9. 初始化购物车商品数据
INSERT INTO wz_cart_items (cart_item_id, cart_id, product_id, quantity, price, product_name, created_time, modified_time) VALUES
(1, 1, 1, 1, 6999.00, 'iPhone 14', NOW(), NOW()),
(2, 1, 3, 2, 299.00, '男士休闲夹克', NOW(), NOW()),
(3, 2, 4, 1, 199.00, '连衣裙', NOW(), NOW());

-- 10. 初始化聊天室数据
INSERT INTO wz_chat_rooms (room_id, product_id, created_time, modified_time) VALUES
(1, 1, NOW(), NOW()),
(2, 2, NOW(), NOW());

-- 11. 初始化聊天消息数据
INSERT INTO wz_chat_messages (room_id, user_id, message, message_type, created_time) VALUES
(1, 2, '这个手机什么时候发货？', 'user', NOW()),
(1, 1, '您好，下单后24小时内发货', 'merchant', NOW()),
(2, 3, '电脑有现货吗？', 'user', NOW()),
(2, 1, '是的，现在有货', 'merchant', NOW());

-- 12. 初始化聊天消息归档数据
INSERT INTO wz_chat_messages_archive (message_id, room_id, user_id, message, message_type, created_time, archived_time) VALUES
(1, 1, 2, '上个月的咨询记录', 'user', DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW()),
(2, 1, 1, '已经回复过的历史消息', 'merchant', DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW());

-- 13. 初始化订单数据（已补全）
INSERT INTO wz_orders (order_id, user_id, total_amount, status, created_time, expire_time) VALUES
(1, 2, 6999.00, 'pending', NOW(), NOW());