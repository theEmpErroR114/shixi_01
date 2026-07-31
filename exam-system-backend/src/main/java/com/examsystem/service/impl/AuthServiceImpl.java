package com.examsystem.service.impl;

import com.examsystem.dto.LoginUserVO;
import com.examsystem.entity.Admin;
import com.examsystem.entity.Student;
import com.examsystem.entity.Teacher;
import com.examsystem.exception.BusinessException;
import com.examsystem.mapper.AdminMapper;
import com.examsystem.mapper.StudentMapper;
import com.examsystem.mapper.TeacherMapper;
import com.examsystem.service.AuthService;
import com.examsystem.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 认证业务实现 — 处理登录和密码修改。
 * 登录流程：
 * 1. 校验参数完整性
 * 2. 根据角色路由到对应的验证方法（admin / teacher / student）
 * 3. 教师和学生额外校验账号状态（status != 0）
 * 4. 密码使用BCrypt比对
 * 5. 返回统一的LoginUserVO（userId、username、realName、role）
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StudentMapper studentMapper;

    /**
     * 用户登录入口。根据role路由到不同的登录逻辑。
     *
     * @param role     角色：admin / teacher / student
     * @param username 用户名
     * @param password 明文密码
     * @return 登录用户信息
     * @throws BusinessException 参数不完整、角色未知、账号密码错误、账号被禁用
     */
    @Override
    public LoginUserVO login(String role, String username, String password) {
        // 参数完整性校验
        if (role == null || username == null || password == null) {
            throw new BusinessException("登录参数不完整");
        }

        // 根据角色路由到对应的登录方法
        switch (role) {
            case "admin":
                return adminLogin(username, password);
            case "teacher":
                return teacherLogin(username, password);
            case "student":
                return studentLogin(username, password);
            default:
                throw new BusinessException("未知的角色类型");
        }
    }

    /**
     * 管理员登录。从t_admin表查询，BCrypt验证密码。
     */
    private LoginUserVO adminLogin(String username, String password) {
        Admin admin = adminMapper.findByUsername(username);
        if (admin == null || !PasswordUtil.matches(password, admin.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        return new LoginUserVO(admin.getAdminId(), admin.getUsername(), admin.getRealName(), "admin");
    }

    /**
     * 教师登录。从t_teacher表查询，额外校验账号是否被禁用（status=0）。
     */
    private LoginUserVO teacherLogin(String username, String password) {
        Teacher teacher = teacherMapper.findByUsername(username);
        if (teacher == null || !PasswordUtil.matches(password, teacher.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        // 校验账号是否被禁用
        if (teacher.getStatus() == null || teacher.getStatus() == 0) {
            throw new BusinessException("该账号已被禁用，请联系管理员");
        }
        return new LoginUserVO(teacher.getTeacherId(), teacher.getUsername(), teacher.getRealName(), "teacher");
    }

    /**
     * 学生登录。从t_student表查询，额外校验账号是否被禁用（status=0）。
     */
    private LoginUserVO studentLogin(String username, String password) {
        Student student = studentMapper.findByUsername(username);
        if (student == null || !PasswordUtil.matches(password, student.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        // 校验账号是否被禁用
        if (student.getStatus() == null || student.getStatus() == 0) {
            throw new BusinessException("该账号已被禁用，请联系管理员");
        }
        return new LoginUserVO(student.getStudentId(), student.getUsername(), student.getRealName(), "student");
    }

    /**
     * 修改密码。流程：
     * 1. 根据角色查询当前用户信息
     * 2. 验证旧密码是否正确（BCrypt比对）
     * 3. 将新密码BCrypt加密后更新到数据库
     *
     * @param role        角色
     * @param userId      用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @throws BusinessException 角色未知或旧密码错误
     */
    @Override
    public void changePassword(String role, Long userId, String oldPassword, String newPassword) {
        // 新密码提前加密一次，避免在各个分支重复加密
        String encodedNew = PasswordUtil.encode(newPassword);
        switch (role) {
            case "admin":
                Admin admin = adminMapper.selectById(userId);
                if (admin == null || !PasswordUtil.matches(oldPassword, admin.getPassword())) {
                    throw new BusinessException("原密码错误");
                }
                adminMapper.updatePassword(userId, encodedNew);
                break;
            case "teacher":
                Teacher teacher = teacherMapper.selectById(userId);
                if (teacher == null || !PasswordUtil.matches(oldPassword, teacher.getPassword())) {
                    throw new BusinessException("原密码错误");
                }
                teacherMapper.updatePassword(userId, encodedNew);
                break;
            case "student":
                Student student = studentMapper.selectById(userId);
                if (student == null || !PasswordUtil.matches(oldPassword, student.getPassword())) {
                    throw new BusinessException("原密码错误");
                }
                studentMapper.updatePassword(userId, encodedNew);
                break;
            default:
                throw new BusinessException("未知的角色类型");
        }
    }
}
