package com.examsystem.util;

/**
 * Session 会话工具类
 * 定义 Session 中存储的键名常量，统一管理避免硬编码字符串
 */
public class SessionUtil {
    /** Session 中存储用户ID的键名 */
    public static final String SESSION_USER_ID = "userId";
    /** Session 中存储用户角色的键名 */
    public static final String SESSION_USER_ROLE = "role";
    /** Session 中存储用户真实姓名的键名 */
    public static final String SESSION_USER_NAME = "realName";
    /** Session 中存储用户名的键名 */
    public static final String SESSION_USERNAME = "username";

    /** 私有构造函数，防止实例化工具类 */
    private SessionUtil() {}
}
