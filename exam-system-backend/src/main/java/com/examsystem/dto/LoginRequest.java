package com.examsystem.dto;

import lombok.Data;

/**
 * 登录请求 DTO
 * 前端 POST /api/auth/login 的请求体
 */
@Data
public class LoginRequest {
    /** 登录角色：admin、teacher、student */
    private String role;
    /** 用户名 */
    private String username;
    /** 密码（明文，后端用 BCrypt 比对） */
    private String password;
}
