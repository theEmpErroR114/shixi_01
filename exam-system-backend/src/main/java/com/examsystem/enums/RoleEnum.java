package com.examsystem.enums;

/**
 * 用户角色枚举
 * 用于登录验证、角色拦截器权限判断
 */
public enum RoleEnum {
    /** 管理员 — 超级用户，可访问所有接口，管理教师和学生账号 */
    ADMIN("admin"),
    /** 教师 — 创建题目和试卷，查看学生成绩统计 */
    TEACHER("teacher"),
    /** 学生 — 练习题目、参加考试、查看成绩 */
    STUDENT("student");

    /** 角色字符串值 */
    private final String value;

    RoleEnum(String value) {
        this.value = value;
    }

    /**
     * 获取角色字符串值
     * @return 角色值（"admin"、"teacher"、"student"）
     */
    public String getValue() {
        return value;
    }

    /**
     * 根据字符串值查找对应的角色枚举
     * @param value 角色字符串
     * @return 对应的 RoleEnum
     * @throws IllegalArgumentException 如果找不到匹配的角色
     */
    public static RoleEnum fromValue(String value) {
        for (RoleEnum role : RoleEnum.values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
