package com.wyc21.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app.database")
@Data
public class DatabaseInitConfig {
    private boolean initialized = false;
} 