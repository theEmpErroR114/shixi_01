package com.examsystem.service;

import com.examsystem.dto.DashboardStatsVO;
import com.examsystem.entity.Student;
import com.examsystem.entity.Teacher;

import java.util.List;

public interface AdminService {
    DashboardStatsVO getDashboardStats();

    // Teacher management
    List<Teacher> listTeachers(String keyword, Integer status, Integer page, Integer pageSize);
    Long countTeachers(String keyword, Integer status);
    Teacher getTeacherById(Long id);
    void createTeacher(Teacher teacher);
    void updateTeacher(Teacher teacher);
    void updateTeacherStatus(Long teacherId, Integer status);
    void resetTeacherPassword(Long teacherId);

    // Student management
    List<Student> listStudents(String keyword, String className, Integer status, Integer page, Integer pageSize);
    Long countStudents(String keyword, String className, Integer status);
    Student getStudentById(Long id);
    void createStudent(Student student);
    void updateStudent(Student student);
    void updateStudentStatus(Long studentId, Integer status);
    void resetStudentPassword(Long studentId);
}
