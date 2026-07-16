package com.examsystem.service.impl;

import com.examsystem.dto.DashboardStatsVO;
import com.examsystem.entity.Student;
import com.examsystem.entity.Teacher;
import com.examsystem.exception.BusinessException;
import com.examsystem.mapper.StudentMapper;
import com.examsystem.mapper.TeacherMapper;
import com.examsystem.service.AdminService;
import com.examsystem.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StudentMapper studentMapper;

    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    public DashboardStatsVO getDashboardStats() {
        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setTeacherCount(teacherMapper.countByKeyword(null, null));
        vo.setActiveTeacherCount(teacherMapper.countByKeyword(null, 1));
        vo.setStudentCount(studentMapper.countByKeyword(null, null, null));
        vo.setActiveStudentCount(studentMapper.countByKeyword(null, null, 1));
        return vo;
    }

    // === Teacher Management ===

    @Override
    public List<Teacher> listTeachers(String keyword, Integer status, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        return teacherMapper.findByKeyword(keyword, status, offset, pageSize);
    }

    @Override
    public Long countTeachers(String keyword, Integer status) {
        return teacherMapper.countByKeyword(keyword, status);
    }

    @Override
    public Teacher getTeacherById(Long id) {
        return teacherMapper.selectById(id);
    }

    @Override
    public void createTeacher(Teacher teacher) {
        Teacher existing = teacherMapper.findByUsername(teacher.getUsername());
        if (existing != null) {
            throw new BusinessException("该用户名已存在");
        }
        teacher.setPassword(PasswordUtil.encode(DEFAULT_PASSWORD));
        teacher.setStatus(1);
        teacher.setCreateTime(LocalDateTime.now());
        teacherMapper.insert(teacher);
    }

    @Override
    public void updateTeacher(Teacher teacher) {
        teacherMapper.update(teacher);
    }

    @Override
    public void updateTeacherStatus(Long teacherId, Integer status) {
        teacherMapper.updateStatus(teacherId, status);
    }

    @Override
    public void resetTeacherPassword(Long teacherId) {
        teacherMapper.updatePassword(teacherId, PasswordUtil.encode(DEFAULT_PASSWORD));
    }

    // === Student Management ===

    @Override
    public List<Student> listStudents(String keyword, String className, Integer status, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        return studentMapper.findByKeyword(keyword, className, status, offset, pageSize);
    }

    @Override
    public Long countStudents(String keyword, String className, Integer status) {
        return studentMapper.countByKeyword(keyword, className, status);
    }

    @Override
    public Student getStudentById(Long id) {
        return studentMapper.selectById(id);
    }

    @Override
    public void createStudent(Student student) {
        Student existing = studentMapper.findByUsername(student.getUsername());
        if (existing != null) {
            throw new BusinessException("该用户名已存在");
        }
        student.setPassword(PasswordUtil.encode(DEFAULT_PASSWORD));
        student.setStatus(1);
        student.setCreateTime(LocalDateTime.now());
        studentMapper.insert(student);
    }

    @Override
    public void updateStudent(Student student) {
        studentMapper.update(student);
    }

    @Override
    public void updateStudentStatus(Long studentId, Integer status) {
        studentMapper.updateStatus(studentId, status);
    }

    @Override
    public void resetStudentPassword(Long studentId) {
        studentMapper.updatePassword(studentId, PasswordUtil.encode(DEFAULT_PASSWORD));
    }
}
