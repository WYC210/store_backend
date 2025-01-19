package com.wyc21.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的域名
        config.addAllowedOrigin("http://localhost:8080");  // 允许本地开发
        config.addAllowedOrigin("http://192.168.0.102:8080"); // 允许局域网访问
        config.addAllowedOrigin("http://192.168.0.102"); // 允许不带端口访问

        // 允许携带认证信息
        config.setAllowCredentials(true);

        // 允许的 HTTP 方法
        config.addAllowedMethod("*");

        // 允许的请求头
        config.addAllowedHeader("*");

        // 暴露响应头
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Set-Cookie");

        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}