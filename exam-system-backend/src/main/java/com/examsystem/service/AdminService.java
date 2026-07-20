package com.examsystem.service;

import com.examsystem.dto.DashboardStatsVO;
import com.examsystem.entity.Course;
import com.examsystem.entity.Student;
import com.examsystem.entity.Teacher;

import java.util.List;
import java.util.Map;

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
    void deleteTeacher(Long teacherId);

    // Student management
    List<Student> listStudents(String keyword, String className, Integer status, Integer page, Integer pageSize);
    Long countStudents(String keyword, String className, Integer status);
    Student getStudentById(Long id);
    void createStudent(Student student);
    void updateStudent(Student student);
    void updateStudentStatus(Long studentId, Integer status);
    void resetStudentPassword(Long studentId);
    void deleteStudent(Long studentId);

    // Course management
    List<Course> listCourses(String keyword, Integer page, Integer pageSize);
    Long countCourses(String keyword);
    Course getCourseById(Long id);
    void createCourse(Course course);
    void updateCourse(Course course);
    void deleteCourse(Long courseId);

    // Teacher-Course assignment
    List<Course> getTeacherCourses(Long teacherId);
    void assignCoursesToTeacher(Long teacherId, List<Long> courseIds);

    // Student-Course assignment
    List<Course> getStudentCourses(Long studentId);
    void assignCoursesToStudent(Long studentId, List<Long> courseIds);
}
