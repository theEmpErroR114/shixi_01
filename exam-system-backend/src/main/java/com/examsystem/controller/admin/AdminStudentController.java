package com.examsystem.controller.admin;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.Result;
import com.examsystem.entity.Course;
import com.examsystem.entity.Student;
import com.examsystem.service.AdminService;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员学生管理控制器 — 提供学生的 CRUD、状态管理、密码重置、课程选课等操作。
 * <p>
 * 学生管理核心功能：
 * <ul>
 *   <li>分页查询学生列表（支持关键词、班级、状态筛选）</li>
 *   <li>创建、编辑学生信息</li>
 *   <li>启用/禁用学生账号（状态切换）</li>
 *   <li>重置学生密码</li>
 *   <li>查询和分配学生选课（多对多关系，通过 t_student_course 表维护）</li>
 *   <li>删除学生（同时清除选课关系，练习和考试记录保留——student_id 外键约束为 SET NULL）</li>
 * </ul>
 *
 * @see com.examsystem.service.AdminService 管理员业务逻辑
 */
@RestController
@RequestMapping("/api/admin/students")
public class AdminStudentController {

    @Autowired
    private AdminService adminService;

    /**
     * 分页查询学生列表，支持关键词、班级、状态筛选。
     *
     * @param keyword  搜索关键词（可选，匹配用户名或真实姓名）
     * @param className 班级名称筛选（可选）
     * @param status   状态筛选：1=启用, 0=禁用（可选）
     * @param page     当前页码（默认第1页）
     * @param pageSize 每页条数（默认10条）
     * @return 分页学生结果
     */
    @GetMapping
    public Result<PageResult<Student>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Student> list = adminService.listStudents(keyword, className, status, page, pageSize);
        Long total = adminService.countStudents(keyword, className, status);
        return Result.success(PageResult.of(total, page, pageSize, list));
    }

    /**
     * 根据 ID 获取单个学生详情。
     *
     * @param id 学生ID
     * @return 学生实体
     */
    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Long id) {
        return Result.success(adminService.getStudentById(id));
    }

    /**
     * 创建新学生。
     * <p>
     * 自动从 session 中获取当前管理员 ID 作为创建者。
     *
     * @param student 学生实体（JSON 请求体）
     * @param session HTTP Session
     * @return 操作成功
     */
    @PostMapping
    public Result<?> create(@RequestBody Student student, HttpSession session) {
        Long adminId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        student.setCreateBy(adminId);
        adminService.createStudent(student);
        return Result.success();
    }

    /**
     * 更新学生信息。
     *
     * @param id      学生ID（路径参数）
     * @param student 学生实体（JSON 请求体）
     * @return 操作成功
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Student student) {
        student.setStudentId(id);
        adminService.updateStudent(student);
        return Result.success();
    }

    /**
     * 启用/禁用学生账号。
     *
     * @param id   学生ID
     * @param body 请求体，含 status 字段（1=启用, 0=禁用）
     * @return 操作成功
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateStudentStatus(id, body.get("status"));
        return Result.success();
    }

    /**
     * 重置学生密码为默认密码。
     *
     * @param id 学生ID
     * @return 操作成功
     */
    @PutMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id) {
        adminService.resetStudentPassword(id);
        return Result.success();
    }

    /**
     * 查询学生已选课程列表。
     *
     * @param id 学生ID
     * @return 学生当前选课列表
     */
    @GetMapping("/{id}/courses")
    public Result<List<Course>> getStudentCourses(@PathVariable Long id) {
        return Result.success(adminService.getStudentCourses(id));
    }

    /**
     * 为学生分配课程（全量替换）。
     * <p>
     * 先删除该学生所有现有选课，再插入新的选课列表。
     *
     * @param id   学生ID
     * @param body 请求体，含 courseIds 数组
     * @return 操作成功
     */
    @PutMapping("/{id}/courses")
    public Result<?> assignCourses(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        adminService.assignCoursesToStudent(id, body.get("courseIds"));
        return Result.success();
    }

    /**
     * 删除学生。
     * <p>
     * 删除操作会清除选课关系（t_student_course 表）。
     * 学生的练习记录和考试记录保留在系统中（t_practice_record 和 t_exam_record 的
     * student_id 字段有外键约束 ON DELETE SET NULL，删除学生后 student_id 置为 NULL）。
     *
     * @param id 学生ID
     * @return 操作成功
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        adminService.deleteStudent(id);
        return Result.success();
    }
}
