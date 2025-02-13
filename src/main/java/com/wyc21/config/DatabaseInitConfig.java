package com.wyc21.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.CommandLineRunner;
import com.wyc21.service.DatabaseInitService;
import lombok.Data;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Configuration
@Data
public class DatabaseInitConfig implements CommandLineRunner {
    @Value("classpath:db/db_config.txt")
    private Resource dbConfigFile;

    @Autowired
    private DatabaseInitService databaseInitService;

    private boolean isDatabaseInitialized() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(dbConfigFile.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("database_initialized=")) {
                    return Boolean.parseBoolean(line.split("=")[1].trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // 默认返回 false
    }

    @Override
    public void run(String... args) {
        if (!isDatabaseInitialized()) {
            databaseInitService.initializeDatabase();
            // 更新配置文件，标记数据库已初始化
            updateDatabaseInitializedFlag(true);
        }
    }

    private void updateDatabaseInitializedFlag(boolean initialized) {
        try (FileWriter writer = new FileWriter(dbConfigFile.getFile())) {
            writer.write("database_initialized=" + initialized + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}