package com.wyc21.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException; // 添加 IOException 导入
import java.nio.charset.StandardCharsets;
import java.util.Arrays; // 添加 Arrays 导入



@Service
@Slf4j
public class DatabaseInitService {

    @Value("classpath:db/schema.sql")
    private Resource schemaSql;

    @Value("classpath:db/data.sql")
    private Resource dataSql;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void initializeDatabase() {
        try {
            log.info("开始初始化数据库...");
            // 清空所有表
            clearDatabase();
           
            // 执行 schema.sql
            String schema = StreamUtils.copyToString(schemaSql.getInputStream(), StandardCharsets.UTF_8);
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
            String data = StreamUtils.copyToString(dataSql.getInputStream(), StandardCharsets.UTF_8);
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

            log.info("数据库初始化完成");
        } catch (IOException e) {
            log.error("数据库初始化失败: {}", e.getMessage());
            throw new RuntimeException("数据库初始化失败", e);
        }
    }
    private void clearDatabase() {
        String[] clearSqls = {
                "SET FOREIGN_KEY_CHECKS = 0",
                "DROP TABLE wz_chat_messages_archive",
                "DROP TABLE wz_chat_messages",
                "DROP TABLE wz_chat_rooms",
                "DROP TABLE wz_order_items",
                "DROP TABLE wz_orders",
                "DROP TABLE wz_cart_items",
                "DROP TABLE wz_carts",
                "DROP TABLE wz_browse_history",
                "DROP TABLE wz_browser_fingerprints",
                "DROP TABLE wz_product_images",
                "DROP TABLE wz_products",
                "DROP TABLE wz_categories",
                "DROP TABLE wz_users",
                "DROP TABLE wz_id_generator",
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