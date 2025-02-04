package com.wyc21.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DatabaseInitService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void initializeDatabase() {
        try {
            logger.info("Starting database initialization...");

            // 1. 禁用外键检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            logger.info("Foreign key checks disabled");

            // 2. 获取所有表名
            List<String> tables = jdbcTemplate.queryForList(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = 'store'",
                    String.class);
            logger.info("Found {} existing tables", tables.size());

            // 3. 删除所有现有表
            for (String table : tables) {
                logger.info("Dropping table: {}", table);
                jdbcTemplate.execute("DROP TABLE IF EXISTS " + table);
            }

            // 4. 启用外键检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            logger.info("Foreign key checks enabled");

            // 5. 读取schema.sql文件内容
            ClassPathResource resource = new ClassPathResource("db/schema.sql");
            String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            logger.info("Schema.sql file loaded");

            // 6. 按照正确的顺序创建表
            // 首先创建没有外键依赖的表
            executeTableCreation(sql, "wz_id_generator");
            executeTableCreation(sql, "wz_users");
            executeTableCreation(sql, "wz_categories");
            executeTableCreation(sql, "wz_products");
            // 然后创建有外键依赖的表
            executeTableCreation(sql, "wz_product_images");
            executeTableCreation(sql, "wz_browser_fingerprints");
            executeTableCreation(sql, "wz_browse_history");
            executeTableCreation(sql, "wz_carts");
            executeTableCreation(sql, "wz_cart_items");
            executeTableCreation(sql, "wz_orders");
            executeTableCreation(sql, "wz_order_items");
            executeTableCreation(sql, "wz_chat_rooms");
            executeTableCreation(sql, "wz_chat_messages");
            executeTableCreation(sql, "wz_chat_messages_archive");

            logger.info("Schema creation completed");

            // 7. 读取并执行data.sql
            ClassPathResource dataResource = new ClassPathResource("db/data.sql");
            String dataSql = StreamUtils.copyToString(dataResource.getInputStream(), StandardCharsets.UTF_8);
            logger.info("Data.sql file loaded");

            // 8. 执行初始化数据的SQL语句
            for (String statement : dataSql.split(";")) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty()) {
                    logger.debug("Executing SQL: {}", trimmedStatement);
                    jdbcTemplate.execute(trimmedStatement);
                }
            }
            logger.info("Data initialization completed");

        } catch (Exception e) {
            logger.error("Database initialization failed", e);
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    private void executeTableCreation(String sql, String tableName) {
        Pattern pattern = Pattern.compile(
            "CREATE TABLE(?: IF NOT EXISTS)? " + tableName + "[^;]+;",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String createTableSql = matcher.group();
            logger.info("Creating table: {}", tableName);
            jdbcTemplate.execute(createTableSql);
        }
    }

    private void dropAllTables() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                String.class);

        for (String table : tables) {
            jdbcTemplate.execute("DROP TABLE IF EXISTS " + table);
        }
    }

    private List<String> splitSqlStatements(String sql) {
        List<String> statements = new ArrayList<>();
        // 修改正则表达式以匹配注释
        Pattern pattern = Pattern.compile(
                "(?:/\\*.*?\\*/\\s*)?CREATE TABLE[^;]+;",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sql);

        while (matcher.find()) {
            statements.add(matcher.group().trim());
        }
        return statements;
    }

    private List<String> orderStatements(List<String> statements) {
        List<String> orderedStatements = new ArrayList<>();
        List<String> remainingStatements = new ArrayList<>(statements);

        // 首先创建没有外键依赖的表
        while (!remainingStatements.isEmpty()) {
            boolean found = false;
            for (int i = 0; i < remainingStatements.size(); i++) {
                String statement = remainingStatements.get(i);
                if (!hasDependency(statement, orderedStatements)) {
                    orderedStatements.add(statement);
                    remainingStatements.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found && !remainingStatements.isEmpty()) {
                // 如果找不到无依赖的表，就把剩下的都加进去
                orderedStatements.addAll(remainingStatements);
                break;
            }
        }

        return orderedStatements;
    }

    private boolean hasDependency(String statement, List<String> existingStatements) {
        // 检查是否有外键依赖
        Pattern pattern = Pattern.compile(
                "FOREIGN KEY.*REFERENCES\\s+`?(\\w+)`?",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(statement);

        while (matcher.find()) {
            String referencedTable = matcher.group(1);
            boolean tableExists = false;

            // 检查被引用的表是否已经创建
            for (String existing : existingStatements) {
                if (existing.contains("CREATE TABLE " + referencedTable) ||
                        existing.contains("CREATE TABLE IF NOT EXISTS " + referencedTable)) {
                    tableExists = true;
                    break;
                }
            }

            if (!tableExists) {
                return true; // 有未满足的依赖
            }
        }

        return false; // 没有未满足的依赖
    }
}