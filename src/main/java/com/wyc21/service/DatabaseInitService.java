package com.wyc21.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import java.io.FileInputStream;
@Service
@Slf4j
public class DatabaseInitService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String dbConfigPath = "src/main/resources/db/db_config.txt"; // 使用相对路径

    public void initializeDatabase() {
        try {
            // 检查是否已初始化
            if (isDatabaseInitialized()) {
                log.info("数据库已初始化，跳过初始化过程");
                return;
            }

            log.info("开始初始化数据库...");

            // 清空所有表
            clearDatabase();

            // 执行 schema.sql
            String schema = StreamUtils.copyToString(
                    new File("src/main/resources/db/schema.sql").toURI().toURL().openStream(), StandardCharsets.UTF_8);
            Arrays.stream(schema.split(";"))
                    .map(String::trim)
                    .filter(sql -> !sql.isEmpty())
                    .forEach(sql -> {
                        try {
                            log.debug("执行SQL: {}", sql);
                            jdbcTemplate.execute(sql);
                        } catch (Exception e) {
                            log.error("执行schema.sql出错: {}", e.getMessage());
                        }
                    });

            // 执行 data.sql
            String data = StreamUtils.copyToString(
                    new File("src/main/resources/db/data.sql").toURI().toURL().openStream(), StandardCharsets.UTF_8);
            Arrays.stream(data.split(";"))
                    .map(String::trim)
                    .filter(sql -> !sql.isEmpty())
                    .forEach(sql -> {
                        try {
                            log.debug("执行SQL: {}", sql);
                            jdbcTemplate.execute(sql);
                        } catch (Exception e) {
                            log.error("执行data.sql出错: {}", e.getMessage());
                        }
                    });

            // 更新配置文件状态
            updateDatabaseInitStatus(true);

            log.info("数据库初始化完成");
        } catch (Exception e) {
            log.error("数据库初始化失败: {}", e.getMessage());
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    private boolean isDatabaseInitialized() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(dbConfigPath)); // 使用相对路径读取配置文件
            String initStatus = props.getProperty("database_initialized", "false");
            log.info("读取到的数据库初始化状态: {}", initStatus);
            return Boolean.parseBoolean(initStatus);
        } catch (Exception e) {
            log.error("读取数据库配置文件失败: {}", e.getMessage());
            return false;
        }
    }

    private void updateDatabaseInitStatus(boolean status) {
        try {
            File configFile = new File(dbConfigPath); // 使用相对路径
            log.info("更新配置文件路径: {}", configFile.getAbsolutePath());

            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write("# 数据库初始化配置\n");
                writer.write("# false 表示未初始化，true 表示已初始化\n");
                writer.write("database_initialized=" + status);
                log.info("成功写入新的初始化状态: {}", status);
            }

            // 验证写入是否成功
            Properties props = new Properties();
            props.load(new FileInputStream(dbConfigPath)); // 使用相对路径读取配置文件
            String newStatus = props.getProperty("database_initialized");
            log.info("验证更新后的状态: {}", newStatus);
        } catch (Exception e) {
            log.error("更新数据库配置文件失败: {}", e.getMessage());
            throw new RuntimeException("更新数据库配置文件失败", e);
        }
    }

    private void clearDatabase() {
        String[] clearSqls = {
                "SET FOREIGN_KEY_CHECKS = 0",
                "DROP TABLE IF EXISTS wz_chat_messages_archive",
                "DROP TABLE IF EXISTS wz_chat_messages",
                "DROP TABLE IF EXISTS wz_chat_rooms",
                "DROP TABLE IF EXISTS wz_order_items",
                "DROP TABLE IF EXISTS wz_orders",
                "DROP TABLE IF EXISTS wz_cart_items",
                "DROP TABLE IF EXISTS wz_carts",
                "DROP TABLE IF EXISTS wz_browse_history",
                "DROP TABLE IF EXISTS wz_browser_fingerprints",
                "DROP TABLE IF EXISTS wz_product_images",
                "DROP TABLE IF EXISTS wz_products",
                "DROP TABLE IF EXISTS wz_categories",
                "DROP TABLE IF EXISTS wz_users",
                "DROP TABLE IF EXISTS wz_id_generator",
                "SET FOREIGN_KEY_CHECKS = 1"
        };

        for (String sql : clearSqls) {
            try {
                log.debug("执行SQL: {}", sql);
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                log.error("执行SQL出错: {}", e.getMessage());
            }
        }
        log.info("数据库已清空");
    }
}