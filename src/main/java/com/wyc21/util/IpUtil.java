package com.wyc21.util;

import jakarta.servlet.http.HttpServletRequest;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import java.io.InputStream;

@Component
public class IpUtil {
    private static Searcher searcher;

    static {
        try {
            // 加载 ip2region.xdb 文件
            ClassPathResource resource = new ClassPathResource("ip2region.xdb");
            InputStream inputStream = resource.getInputStream();
            byte[] cBuff = FileCopyUtils.copyToByteArray(inputStream);
            searcher = Searcher.newWithBuffer(cBuff);
        } catch (Exception e) {
            throw new RuntimeException("IP地理位置数据库加载失败", e);
        }
    }

    public String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public String getIpLocation(String ip) {
        try {
            return searcher.search(ip);
        } catch (Exception e) {
            return "unknown";
        }
    }
}