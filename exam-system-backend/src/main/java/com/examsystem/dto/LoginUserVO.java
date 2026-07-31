package com.examsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录用户信息 VO
 * 登录成功后返回给前端，存入 session 并在页面头部展示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserVO {
    /** 用户ID（adminId/teacherId/studentId 统一映射为此字段） */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 真实姓名 */
    private String realName;
    /** 角色：admin、teacher、student */
    private String role;
}
