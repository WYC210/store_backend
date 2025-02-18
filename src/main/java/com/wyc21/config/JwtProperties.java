package com.wyc21.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtProperties {

    @NotBlank(message = "JWT secret cannot be empty")
    private String secret;

    @Positive(message = "Access token expiration must be positive")
    private Long accessTokenExpiration;

    @Positive(message = "Refresh token expiration must be positive")
    private Long refreshTokenExpiration;

    // Getters and Setters (如果不使用 Lombok 的 @Data 注解，则需要手动添加以下方法)
    /*
     * public String getSecret() {
     * return secret;
     * }
     * 
     * public void setSecret(String secret) {
     * this.secret = secret;
     * }
     * 
     * public Long getAccessTokenExpiration() {
     * return accessTokenExpiration;
     * }
     * 
     * public void setAccessTokenExpiration(Long accessTokenExpiration) {
     * this.accessTokenExpiration = accessTokenExpiration;
     * }
     * 
     * public Long getRefreshTokenExpiration() {
     * return refreshTokenExpiration;
     * }
     * 
     * public void setRefreshTokenExpiration(Long refreshTokenExpiration) {
     * this.refreshTokenExpiration = refreshTokenExpiration;
     * }
     */
}