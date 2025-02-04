package com.wyc21.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DatabaseInitService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Transactional
    public void initializeDatabase() {
        try {
            // 1. 禁用外键检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // 2. 删除所有现有表
            dropAllTables();
            
            // 3. 读取schema.sql文件内容
            ClassPathResource resource = new ClassPathResource("db/schema.sql");
            String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            
            // 4. 分割SQL语句并按正确顺序执行
            List<String> statements = splitSqlStatements(sql);
            List<String> orderedStatements = orderStatements(statements);
            
            // 5. 执行SQL语句
            for (String statement : orderedStatements) {
                if (!statement.trim().isEmpty()) {
                    jdbcTemplate.execute(statement);
                }
            }
            
            // 6. 启用外键检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            
        } catch (Exception e) {
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }
    
    private void dropAllTables() {
        List<String> tables = jdbcTemplate.queryForList(
            "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
            String.class
        );
        
        for (String table : tables) {
            jdbcTemplate.execute("DROP TABLE IF EXISTS " + table);
        }
    }
    
    private List<String> splitSqlStatements(String sql) {
        List<String> statements = new ArrayList<>();
        // 修改正则表达式以匹配注释
        Pattern pattern = Pattern.compile(
            "(?:/\\*.*?\\*/\\s*)?CREATE TABLE[^;]+;",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
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
            Pattern.CASE_INSENSITIVE
        );
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