package com.examsystem.service;

import com.examsystem.dto.LoginUserVO;

/**
 * 认证业务接口 — 负责用户登录和密码修改。
 * 支持三种角色：admin（管理员）、teacher（教师）、student（学生）。
 */
public interface AuthService {

    /**
     * 用户登录。根据角色路由到对应的登录逻辑：
     * admin → 查询t_admin表，teacher → 查询t_teacher表，student → 查询t_student表。
     * 教师和学生登录会额外校验账号是否被禁用（status=0）。
     * 密码使用BCrypt进行匹配校验。
     *
     * @param role     角色类型：admin / teacher / student
     * @param username 用户名
     * @param password 明文密码
     * @return 登录成功返回用户信息（userId、username、realName、role）
     * @throws BusinessException 如果参数不完整、角色未知、账号/密码错误或账号被禁用
     */
    LoginUserVO login(String role, String username, String password);

    /**
     * 修改当前用户的密码。先验证旧密码是否正确，再更新为新密码（BCrypt加密）。
     *
     * @param role        角色类型：admin / teacher / student
     * @param userId      用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @throws BusinessException 如果角色未知或旧密码错误
     */
    void changePassword(String role, Long userId, String oldPassword, String newPassword);
}
