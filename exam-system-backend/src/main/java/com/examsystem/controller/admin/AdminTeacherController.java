package com.examsystem.controller.admin;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.Result;
import com.examsystem.entity.Course;
import com.examsystem.entity.Teacher;
import com.examsystem.service.AdminService;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员教师管理控制器 — 提供教师的 CRUD、状态管理、密码重置、课程分配等操作。
 * <p>
 * 教师管理核心功能：
 * <ul>
 *   <li>分页查询教师列表（支持关键词、状态筛选）</li>
 *   <li>创建、编辑教师信息</li>
 *   <li>启用/禁用教师账号（状态切换）</li>
 *   <li>重置教师密码</li>
 *   <li>查询和分配教师所授课程（多对多关系，通过 t_teacher_course 表维护）</li>
 *   <li>删除教师（同时清除课程分配关系，但教师创建的题目和试卷保留——teacher_id 无外键约束）</li>
 * </ul>
 *
 * @see com.examsystem.service.AdminService 管理员业务逻辑
 */
@RestController
@RequestMapping("/api/admin/teachers")
public class AdminTeacherController {

    @Autowired
    private AdminService adminService;

    /**
     * 分页查询教师列表，支持关键词搜索和状态筛选。
     *
     * @param keyword  搜索关键词（可选，匹配用户名或真实姓名）
     * @param status   状态筛选：1=启用, 0=禁用（可选）
     * @param page     当前页码（默认第1页）
     * @param pageSize 每页条数（默认10条）
     * @return 分页教师结果
     */
    @GetMapping
    public Result<PageResult<Teacher>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Teacher> list = adminService.listTeachers(keyword, status, page, pageSize);
        Long total = adminService.countTeachers(keyword, status);
        return Result.success(PageResult.of(total, page, pageSize, list));
    }

    /**
     * 根据 ID 获取单个教师详情。
     *
     * @param id 教师ID
     * @return 教师实体
     */
    @GetMapping("/{id}")
    public Result<Teacher> getById(@PathVariable Long id) {
        return Result.success(adminService.getTeacherById(id));
    }

    /**
     * 创建新教师。
     * <p>
     * 自动从 session 中获取当前管理员 ID 作为创建者。
     *
     * @param teacher 教师实体（JSON 请求体）
     * @param session HTTP Session，用于获取当前管理员 ID
     * @return 操作成功
     */
    @PostMapping
    public Result<?> create(@RequestBody Teacher teacher, HttpSession session) {
        Long adminId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        teacher.setCreateBy(adminId); // 记录创建者
        adminService.createTeacher(teacher);
        return Result.success();
    }

    /**
     * 更新教师信息。
     *
     * @param id      教师ID（路径参数）
     * @param teacher 教师实体（JSON 请求体）
     * @return 操作成功
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Teacher teacher) {
        teacher.setTeacherId(id);
        adminService.updateTeacher(teacher);
        return Result.success();
    }

    /**
     * 启用/禁用教师账号。
     *
     * @param id   教师ID
     * @param body 请求体，含 status 字段（1=启用, 0=禁用）
     * @return 操作成功
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateTeacherStatus(id, body.get("status"));
        return Result.success();
    }

    /**
     * 重置教师密码为默认密码。
     *
     * @param id 教师ID
     * @return 操作成功
     */
    @PutMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id) {
        adminService.resetTeacherPassword(id);
        return Result.success();
    }

    /**
     * 查询教师已分配的课程列表。
     *
     * @param id 教师ID
     * @return 教师当前所授课程列表
     */
    @GetMapping("/{id}/courses")
    public Result<List<Course>> getTeacherCourses(@PathVariable Long id) {
        return Result.success(adminService.getTeacherCourses(id));
    }

    /**
     * 为教师分配课程（全量替换）。
     * <p>
     * 先删除该教师所有现有课程分配，再插入新的课程分配列表。
     *
     * @param id   教师ID
     * @param body 请求体，含 courseIds 数组
     * @return 操作成功
     */
    @PutMapping("/{id}/courses")
    public Result<?> assignCourses(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        adminService.assignCoursesToTeacher(id, body.get("courseIds"));
        return Result.success();
    }

    /**
     * 删除教师。
     * <p>
     * 删除操作会清除课程分配关系（t_teacher_course 表），
     * 但教师创建的题目和试卷保留在系统中（teacher_id 字段无外键约束，仅为展示字段）。
     *
     * @param id 教师ID
     * @return 操作成功
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        adminService.deleteTeacher(id);
        return Result.success();
    }
}
