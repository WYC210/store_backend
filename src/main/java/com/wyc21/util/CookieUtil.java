package com.wyc21.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${app.security.cookie-domain}")
    private String cookieDomain;

    @Value("${app.security.secure-cookie}")
    private boolean secureCookie;

    @Value("${app.security.same-site:Lax}")
    private String sameSite;

    public void setTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("AUTH-TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setDomain(cookieDomain);
        cookie.setPath("/");
        cookie.setMaxAge(300); // 5分钟

        // 设置SameSite属性通过响应头
        String cookieHeader = String.format("%s=%s; Path=%s; Domain=%s; Max-Age=%d; %sHttpOnly; SameSite=%s",
                cookie.getName(),
                cookie.getValue(),
                cookie.getPath(),
                cookie.getDomain(),
                cookie.getMaxAge(),
                cookie.getSecure() ? "Secure; " : "",
                sameSite);

        response.setHeader("Set-Cookie", cookieHeader);
    }

    public void clearTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("AUTH-TOKEN", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setDomain(cookieDomain);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        // 设置SameSite属性通过响应头
        String cookieHeader = String.format("%s=%s; Path=%s; Domain=%s; Max-Age=%d; %sHttpOnly; SameSite=%s",
                cookie.getName(),
                cookie.getValue(),
                cookie.getPath(),
                cookie.getDomain(),
                cookie.getMaxAge(),
                cookie.getSecure() ? "Secure; " : "",
                sameSite);

        response.setHeader("Set-Cookie", cookieHeader);
    }
}