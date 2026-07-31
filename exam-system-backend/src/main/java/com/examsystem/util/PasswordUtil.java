package com.examsystem.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 * 使用 BCrypt 算法进行密码加密和验证，避免明文存储密码
 */
public class PasswordUtil {
    /** BCrypt 编码器（单例复用） */
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 对明文密码进行 BCrypt 加密
     * @param rawPassword 明文密码
     * @return BCrypt 密文（60 字符，含随机盐值）
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证明文密码是否与 BCrypt 密文匹配
     * @param rawPassword     用户输入的明文密码
     * @param encodedPassword 数据库中存储的 BCrypt 密文
     * @return true 表示匹配，false 表示不匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
