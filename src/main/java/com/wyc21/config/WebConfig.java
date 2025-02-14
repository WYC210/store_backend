package com.wyc21.config;

import com.wyc21.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns( // 不拦截的请求

                        "/users/login", // 登录
                        "/users/reg", // 注册
                        "/products/**", // 商品相关的所有接口
                        "/categories",// 分类相关的所有接口
                        "/products/images/**" // 图片相关的所有接口

                );
    }
}