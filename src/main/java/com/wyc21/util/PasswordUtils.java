package com.wyc21.util;


import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    // 加密密码
    public static String hashPassword(String plainPassword) {
        // 生成盐并加密，默认进行10次加密
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    // 验证密码
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}