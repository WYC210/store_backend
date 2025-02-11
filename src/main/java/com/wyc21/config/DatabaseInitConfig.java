package com.wyc21.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import com.wyc21.service.DatabaseInitService;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app.database")
@Data
public class DatabaseInitConfig implements CommandLineRunner {
    /**
     * 数据库是否已初始化
     * 默认为false，表示需要初始化
     * 初始化完成后会被设置为true
     */
    private boolean initialized = false;

    /**
     * 是否强制初始化
     * 设置为true时，每次启动都会重新初始化数据库
     */
    private boolean forceInit = true;

    @Autowired
    private DatabaseInitService databaseInitService;

    @Override
    public void run(String... args) {
        // 如果未初始化或强制初始化，则执行初始化
        if (!initialized || forceInit) {
            databaseInitService.initializeDatabase();
            initialized = true;
        }
    }
  
}