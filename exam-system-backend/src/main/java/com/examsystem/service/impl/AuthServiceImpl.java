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

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public LoginUserVO login(String role, String username, String password) {
        if (role == null || username == null || password == null) {
            throw new BusinessException("登录参数不完整");
        }

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

    private LoginUserVO adminLogin(String username, String password) {
        Admin admin = adminMapper.findByUsername(username);
        if (admin == null || !PasswordUtil.matches(password, admin.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        return new LoginUserVO(admin.getAdminId(), admin.getUsername(), admin.getRealName(), "admin");
    }

    private LoginUserVO teacherLogin(String username, String password) {
        Teacher teacher = teacherMapper.findByUsername(username);
        if (teacher == null || !PasswordUtil.matches(password, teacher.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        if (teacher.getStatus() == null || teacher.getStatus() == 0) {
            throw new BusinessException("该账号已被禁用，请联系管理员");
        }
        return new LoginUserVO(teacher.getTeacherId(), teacher.getUsername(), teacher.getRealName(), "teacher");
    }

    private LoginUserVO studentLogin(String username, String password) {
        Student student = studentMapper.findByUsername(username);
        if (student == null || !PasswordUtil.matches(password, student.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        if (student.getStatus() == null || student.getStatus() == 0) {
            throw new BusinessException("该账号已被禁用，请联系管理员");
        }
        return new LoginUserVO(student.getStudentId(), student.getUsername(), student.getRealName(), "student");
    }
}
