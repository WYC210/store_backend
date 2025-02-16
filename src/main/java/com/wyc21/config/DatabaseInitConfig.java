package com.wyc21.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import com.wyc21.service.DatabaseInitService;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DatabaseInitConfig implements CommandLineRunner {

    @Autowired
    private DatabaseInitService databaseInitService;

    @Override
    public void run(String... args) {
        log.info("开始检查数据库初始化状态...");
        databaseInitService.initializeDatabase();
    }
}