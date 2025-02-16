-- 检查表结构
SHOW CREATE TABLE wz_cart_items;

-- 使用不同的查询方式
SELECT * FROM wz_cart_items WHERE cart_item_id = 679028085021999100;
SELECT * FROM wz_cart_items WHERE cart_item_id = '679028085021999100';
SELECT * FROM wz_cart_items WHERE CAST(cart_item_id AS CHAR) = '679028085021999100';
SELECT * FROM wz_cart_items WHERE cart_item_id = CAST('679028085021999100' AS UNSIGNED);

-- 查看所有数据
SELECT cart_item_id, CAST(cart_item_id AS CHAR) as id_str FROM wz_cart_items;