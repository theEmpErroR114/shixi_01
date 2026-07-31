package com.examsystem.service.impl;

import com.examsystem.dto.DashboardStatsVO;
import com.examsystem.entity.Course;
import com.examsystem.entity.Student;
import com.examsystem.entity.Teacher;
import com.examsystem.exception.BusinessException;
import com.examsystem.mapper.*;
import com.examsystem.service.AdminService;
import com.examsystem.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员业务实现 — 统一管理教师、学生、课程及其分配关系。
 * 核心职责：
 * 1. 仪表盘统计：统计教师/学生的总数和活跃数
 * 2. 教师管理：CRUD + 状态/密码管理 + 课程分配
 * 3. 学生管理：CRUD + 状态/密码管理 + 选修课程分配
 * 4. 课程管理：CRUD（删除前校验题目/试卷关联）+ 教师-课程/学生-课程分配
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private TeacherCourseMapper teacherCourseMapper;

    @Autowired
    private StudentCourseMapper studentCourseMapper;

    /** 新建教师/学生的默认密码 */
    private static final String DEFAULT_PASSWORD = "123456";

    /**
     * 获取仪表盘统计数据：统计教师总数、活跃教师数、学生总数、活跃学生数。
     * 活跃 = status = 1（启用状态）。
     *
     * @return 仪表盘统计数据VO
     */
    @Override
    public DashboardStatsVO getDashboardStats() {
        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setTeacherCount(teacherMapper.countByKeyword(null, null));
        vo.setActiveTeacherCount(teacherMapper.countByKeyword(null, 1));
        vo.setStudentCount(studentMapper.countByKeyword(null, null, null));
        vo.setActiveStudentCount(studentMapper.countByKeyword(null, null, 1));
        return vo;
    }

    // ==================== 教师管理 ====================

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

    /**
     * 创建教师。校验用户名唯一性，密码使用BCrypt加密存储，初始状态为启用(1)。
     *
     * @param teacher 教师实体（需包含username和realName）
     * @throws BusinessException 如果用户名已存在
     */
    @Override
    public void createTeacher(Teacher teacher) {
        // 校验用户名是否已被占用
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

    /**
     * 重置教师密码为默认密码（BCrypt加密后存储）。
     *
     * @param teacherId 教师ID
     */
    @Override
    public void resetTeacherPassword(Long teacherId) {
        teacherMapper.updatePassword(teacherId, PasswordUtil.encode(DEFAULT_PASSWORD));
    }

    /**
     * 删除教师。事务保证两步操作原子性：
     * 1. 删除教师-课程关联（t_teacher_course）
     * 2. 删除教师记录
     * 注意：题目和试卷的teacher_id没有外键约束，仅作展示字段，不会因删除教师而受影响。
     *
     * @param teacherId 教师ID
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteTeacher(Long teacherId) {
        // 清理教师-课程关联
        teacherCourseMapper.deleteByTeacherId(teacherId);
        // 删除教师（题目和试卷的 teacher_id 无外键约束，仅作展示字段，不受影响）
        teacherMapper.deleteById(teacherId);
    }

    // ==================== 学生管理 ====================

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

    /**
     * 创建学生。校验用户名唯一性，密码BCrypt加密，初始状态为启用(1)。
     *
     * @param student 学生实体
     * @throws BusinessException 如果用户名已存在
     */
    @Override
    public void createStudent(Student student) {
        // 校验用户名是否已被占用
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

    /**
     * 重置学生密码为默认密码（BCrypt加密后存储）。
     *
     * @param studentId 学生ID
     */
    @Override
    public void resetStudentPassword(Long studentId) {
        studentMapper.updatePassword(studentId, PasswordUtil.encode(DEFAULT_PASSWORD));
    }

    /**
     * 删除学生。事务保证两步操作原子性：
     * 1. 删除学生-课程关联（t_student_course）
     * 2. 删除学生记录
     * 注意：练习记录和考试记录的student_id由数据库ON DELETE SET NULL自动置空，记录本身保留。
     *
     * @param studentId 学生ID
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteStudent(Long studentId) {
        // 清理学生-课程关联
        studentCourseMapper.deleteByStudentId(studentId);
        // 删除学生（练习记录和考试记录的 student_id 会由数据库 ON DELETE SET NULL 自动置空）
        studentMapper.deleteById(studentId);
    }

    // ==================== 课程管理 ====================

    @Override
    public List<Course> listCourses(String keyword, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        return courseMapper.findByKeyword(keyword, offset, pageSize);
    }

    @Override
    public Long countCourses(String keyword) {
        return courseMapper.countByKeyword(keyword);
    }

    @Override
    public Course getCourseById(Long id) {
        return courseMapper.selectById(id);
    }

    @Override
    public void createCourse(Course course) {
        course.setCreateTime(LocalDateTime.now());
        courseMapper.insert(course);
    }

    @Override
    public void updateCourse(Course course) {
        courseMapper.update(course);
    }

    /**
     * 删除课程。需要满足以下条件才能删除，否则抛出异常：
     * 1. 课程下没有题目（t_question）
     * 2. 课程下没有试卷（t_paper）
     * 删除时先清理关联表（t_teacher_course、t_student_course），再删除课程本身。
     *
     * @param courseId 课程ID
     * @throws BusinessException 如果课程下存在题目或试卷
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteCourse(Long courseId) {
        // 检查课程下是否有关联题目
        Long questionCount = questionMapper.countByFilters(courseId, null, null, null);
        if (questionCount > 0) {
            throw new BusinessException("该课程下还有 " + questionCount + " 道题目，无法删除");
        }
        // 检查课程下是否有关联试卷
        Long paperCount = paperMapper.countByFilters(null, courseId);
        if (paperCount > 0) {
            throw new BusinessException("该课程下还有 " + paperCount + " 份试卷，无法删除");
        }
        // 清理课程关联：教师-课程、学生-课程
        teacherCourseMapper.deleteByCourseId(courseId);
        studentCourseMapper.deleteByCourseId(courseId);
        courseMapper.deleteById(courseId);
    }

    // ==================== 教师-课程分配 ====================

    @Override
    public List<Course> getTeacherCourses(Long teacherId) {
        return teacherCourseMapper.selectCoursesByTeacherId(teacherId);
    }

    /**
     * 为教师分配课程。采用"先删后增"策略：先删除该教师所有旧的课程关联，再逐条插入新的关联。
     * 这样可以避免逐一比对差异的复杂性。
     *
     * @param teacherId 教师ID
     * @param courseIds 要分配的课程ID列表
     */
    @Override
    public void assignCoursesToTeacher(Long teacherId, List<Long> courseIds) {
        // 先清空该教师的所有课程关联
        teacherCourseMapper.deleteByTeacherId(teacherId);
        // 再逐条插入新的课程关联
        if (courseIds != null) {
            for (Long courseId : courseIds) {
                teacherCourseMapper.insert(teacherId, courseId);
            }
        }
    }

    // ==================== 学生-课程分配 ====================

    @Override
    public List<Course> getStudentCourses(Long studentId) {
        return studentCourseMapper.selectCoursesByStudentId(studentId);
    }

    /**
     * 为学生分配（选修）课程。同样采用"先删后增"策略。
     *
     * @param studentId 学生ID
     * @param courseIds 要选修的课程ID列表
     */
    @Override
    public void assignCoursesToStudent(Long studentId, List<Long> courseIds) {
        // 先清空该学生的所有课程选修关联
        studentCourseMapper.deleteByStudentId(studentId);
        // 再逐条插入新的选修关联
        if (courseIds != null) {
            for (Long courseId : courseIds) {
                studentCourseMapper.insert(studentId, courseId);
            }
        }
    }
}
