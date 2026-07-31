package com.examsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生实体类，对应 t_student 表
 */
@Data
public class Student {
    /** 学生ID（主键，自增） */
    private Long studentId;
    /** 登录用户名，唯一 */
    private String username;
    /** 密码（BCrypt加密存储） */
    private String password;
    /** 真实姓名 */
    private String realName;
    /** 性别：M=男, F=女 */
    private String gender;
    /** 班级名称 */
    private String className;
    /** 联系电话 */
    private String phone;
    /** 账号状态：1=启用, 0=禁用 */
    private Integer status;
    /** 创建者ID（管理员ID） */
    private Long createBy;
    /** 创建时间 */
    private LocalDateTime createTime;
}
