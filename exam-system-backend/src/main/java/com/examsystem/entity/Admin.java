package com.examsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员实体类，对应 t_admin 表
 */
@Data
public class Admin {
    /** 管理员ID（主键，自增） */
    private Long adminId;
    /** 登录用户名，唯一 */
    private String username;
    /** 密码（BCrypt加密存储） */
    private String password;
    /** 真实姓名 */
    private String realName;
    /** 联系电话 */
    private String phone;
    /** 创建时间 */
    private LocalDateTime createTime;
}
