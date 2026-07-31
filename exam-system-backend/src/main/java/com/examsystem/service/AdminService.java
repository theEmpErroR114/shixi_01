package com.examsystem.service;

import com.examsystem.dto.DashboardStatsVO;
import com.examsystem.entity.Course;
import com.examsystem.entity.Student;
import com.examsystem.entity.Teacher;

import java.util.List;
import java.util.Map;

/**
 * 管理员业务接口 — 负责后台管理功能：
 * 仪表盘统计、教师管理、学生管理、课程管理、课程分配（教师-课程 / 学生-课程）。
 */
public interface AdminService {

    /**
     * 获取管理员仪表盘统计数据（教师数、活跃教师数、学生数、活跃学生数）。
     *
     * @return 仪表盘统计数据对象
     */
    DashboardStatsVO getDashboardStats();

    // ==================== 教师管理 ====================

    /**
     * 分页查询教师列表，支持按关键词和状态筛选。
     *
     * @param keyword  搜索关键词（用户名或姓名，可为null）
     * @param status   账号状态：1=启用，0=禁用，null=全部
     * @param page     页码（从1开始）
     * @param pageSize 每页条数
     * @return 教师列表
     */
    List<Teacher> listTeachers(String keyword, Integer status, Integer page, Integer pageSize);

    /**
     * 统计符合条件的教师总数。
     *
     * @param keyword 搜索关键词
     * @param status  账号状态
     * @return 教师总数
     */
    Long countTeachers(String keyword, Integer status);

    /**
     * 根据ID获取单个教师信息。
     *
     * @param id 教师ID
     * @return 教师实体，不存在则返回null
     */
    Teacher getTeacherById(Long id);

    /**
     * 创建新教师，密码默认为123456（BCrypt加密），状态默认为启用。
     *
     * @param teacher 教师实体（需包含username和realName）
     * @throws BusinessException 如果用户名已存在
     */
    void createTeacher(Teacher teacher);

    /**
     * 更新教师基本信息（姓名等），不包含密码和状态。
     *
     * @param teacher 教师实体（需设置teacherId及要更新的字段）
     */
    void updateTeacher(Teacher teacher);

    /**
     * 启用或禁用教师账号。
     *
     * @param teacherId 教师ID
     * @param status    目标状态：1=启用，0=禁用
     */
    void updateTeacherStatus(Long teacherId, Integer status);

    /**
     * 重置教师密码为默认密码"123456"（BCrypt加密）。
     *
     * @param teacherId 教师ID
     */
    void resetTeacherPassword(Long teacherId);

    /**
     * 删除教师。先清理教师-课程关联表，再删除教师记录。
     * 注意：题目和试卷的teacher_id仅作展示字段，无外键约束，不会级联删除。
     *
     * @param teacherId 教师ID
     */
    void deleteTeacher(Long teacherId);

    // ==================== 学生管理 ====================

    /**
     * 分页查询学生列表，支持按关键词、班级、状态筛选。
     *
     * @param keyword   搜索关键词（用户名或姓名，可为null）
     * @param className 班级名称（可为null）
     * @param status    账号状态：1=启用，0=禁用，null=全部
     * @param page      页码（从1开始）
     * @param pageSize  每页条数
     * @return 学生列表
     */
    List<Student> listStudents(String keyword, String className, Integer status, Integer page, Integer pageSize);

    /**
     * 统计符合条件的学生总数。
     *
     * @param keyword   搜索关键词
     * @param className 班级名称
     * @param status    账号状态
     * @return 学生总数
     */
    Long countStudents(String keyword, String className, Integer status);

    /**
     * 根据ID获取单个学生信息。
     *
     * @param id 学生ID
     * @return 学生实体，不存在则返回null
     */
    Student getStudentById(Long id);

    /**
     * 创建新学生，密码默认为123456（BCrypt加密），状态默认为启用。
     *
     * @param student 学生实体
     * @throws BusinessException 如果用户名已存在
     */
    void createStudent(Student student);

    /**
     * 更新学生基本信息。
     *
     * @param student 学生实体（需设置studentId及要更新的字段）
     */
    void updateStudent(Student student);

    /**
     * 启用或禁用学生账号。
     *
     * @param studentId 学生ID
     * @param status    目标状态：1=启用，0=禁用
     */
    void updateStudentStatus(Long studentId, Integer status);

    /**
     * 重置学生密码为默认密码"123456"（BCrypt加密）。
     *
     * @param studentId 学生ID
     */
    void resetStudentPassword(Long studentId);

    /**
     * 删除学生。先清理学生-课程关联表，再删除学生记录。
     * 练习记录和考试记录的student_id由数据库ON DELETE SET NULL自动置空，记录本身保留。
     *
     * @param studentId 学生ID
     */
    void deleteStudent(Long studentId);

    // ==================== 课程管理 ====================

    /**
     * 分页查询课程列表，支持关键词搜索。
     *
     * @param keyword  搜索关键词（课程名称，可为null）
     * @param page     页码（从1开始）
     * @param pageSize 每页条数
     * @return 课程列表
     */
    List<Course> listCourses(String keyword, Integer page, Integer pageSize);

    /**
     * 统计符合条件的课程总数。
     *
     * @param keyword 搜索关键词
     * @return 课程总数
     */
    Long countCourses(String keyword);

    /**
     * 根据ID获取单个课程信息。
     *
     * @param id 课程ID
     * @return 课程实体，不存在则返回null
     */
    Course getCourseById(Long id);

    /**
     * 创建新课程。
     *
     * @param course 课程实体
     */
    void createCourse(Course course);

    /**
     * 更新课程信息。
     *
     * @param course 课程实体
     */
    void updateCourse(Course course);

    /**
     * 删除课程。删除前校验：如果课程下存在题目或试卷则拒绝删除。
     * 清理顺序：教师-课程关联 → 学生-课程关联 → 课程记录。
     *
     * @param courseId 课程ID
     * @throws BusinessException 如果课程下存在题目或试卷
     */
    void deleteCourse(Long courseId);

    // ==================== 教师-课程分配 ====================

    /**
     * 获取指定教师已分配的课程列表。
     *
     * @param teacherId 教师ID
     * @return 已分配的课程列表
     */
    List<Course> getTeacherCourses(Long teacherId);

    /**
     * 为教师分配课程（先删除旧关联，再批量插入新关联）。
     *
     * @param teacherId 教师ID
     * @param courseIds 课程ID列表
     */
    void assignCoursesToTeacher(Long teacherId, List<Long> courseIds);

    // ==================== 学生-课程分配 ====================

    /**
     * 获取指定学生已选修的课程列表。
     *
     * @param studentId 学生ID
     * @return 已选修的课程列表
     */
    List<Course> getStudentCourses(Long studentId);

    /**
     * 为学生分配课程（先删除旧关联，再批量插入新关联）。
     *
     * @param studentId 学生ID
     * @param courseIds 课程ID列表
     */
    void assignCoursesToStudent(Long studentId, List<Long> courseIds);
}
