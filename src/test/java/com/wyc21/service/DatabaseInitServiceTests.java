package com.wyc21.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseInitServiceTests {

    @Autowired
    private DatabaseInitService databaseInitService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private final List<String> expectedTables = Arrays.asList(
            "wz_users", "wz_categories", "wz_products", "wz_orders",
            "wz_order_items", "wz_carts", "wz_cart_items", "wz_product_images",
            "wz_chat_rooms", "wz_chat_messages", "wz_chat_messages_archive",
            "wz_browser_fingerprints", "wz_browse_history", "wz_id_generator");

    @BeforeEach
    void setUp() {
        // 验证数据库连接
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Database connection should be valid");

            // 确保测试前数据库是空的
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

            // 获取所有表名
            List<String> existingTables = jdbcTemplate.queryForList(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                    String.class);

            // 删除存在的表
            for (String table : existingTables) {
                jdbcTemplate.execute("DROP TABLE IF EXISTS " + table);
            }

            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (Exception e) {
            fail("Database connection failed: " + e.getMessage());
        }
    }

    @Test
    void testDatabaseInitialization() {
        // 执行初始化
        assertDoesNotThrow(() -> databaseInitService.initializeDatabase(),
                "Database initialization should not throw any exception");

        // 验证表是否创建成功
        for (String tableName : expectedTables) {
            boolean tableExists = doesTableExist(tableName);
            assertTrue(tableExists, "Table " + tableName + " should exist");
        }

        // 验证表结构
        verifyTableStructure();
    }

    @Test
    void testReInitialization() {
        // 首次初始化
        databaseInitService.initializeDatabase();

        // 验证表存在
        for (String tableName : expectedTables) {
            assertTrue(doesTableExist(tableName), "Table should exist after first initialization");
        }

        // 重复初始化
        assertDoesNotThrow(() -> databaseInitService.initializeDatabase(),
                "Re-initialization should not throw any exception");

        // 再次验证表存在
        for (String tableName : expectedTables) {
            assertTrue(doesTableExist(tableName), "Table should still exist after re-initialization");
        }
    }

    private boolean doesTableExist(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName);
        return count != null && count > 0;
    }

    private void verifyTableStructure() {
        // 验证几个关键表的结构
        // 1. 验证用户表结构
        assertTrue(doesColumnExist("wz_users", "uid"), "uid column should exist in users table");
        assertTrue(doesColumnExist("wz_users", "username"), "username column should exist in users table");
        assertTrue(doesColumnExist("wz_users", "password"), "password column should exist in users table");

        // 2. 验证商品表结构
        assertTrue(doesColumnExist("wz_products", "product_id"), "product_id column should exist in products table");
        assertTrue(doesColumnExist("wz_products", "name"), "name column should exist in products table");
        assertTrue(doesColumnExist("wz_products", "price"), "price column should exist in products table");

        // 3. 验证外键约束
        assertTrue(doesForeignKeyExist("wz_products", "category_id", "wz_categories"),
                "Foreign key from products to categories should exist");
    }

    private boolean doesColumnExist(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() " +
                        "AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName, columnName);
        return count != null && count > 0;
    }

    private boolean doesForeignKeyExist(String tableName, String columnName, String referencedTable) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.key_column_usage " +
                        "WHERE table_schema = DATABASE() AND table_name = ? " +
                        "AND column_name = ? AND referenced_table_name = ?",
                Integer.class,
                tableName, columnName, referencedTable);
        return count != null && count > 0;
    }
}