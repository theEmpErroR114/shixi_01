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

@RestController
@RequestMapping("/api/admin/teachers")
public class AdminTeacherController {

    @Autowired
    private AdminService adminService;

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

    @GetMapping("/{id}")
    public Result<Teacher> getById(@PathVariable Long id) {
        return Result.success(adminService.getTeacherById(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody Teacher teacher, HttpSession session) {
        Long adminId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        teacher.setCreateBy(adminId);
        adminService.createTeacher(teacher);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Teacher teacher) {
        teacher.setTeacherId(id);
        adminService.updateTeacher(teacher);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateTeacherStatus(id, body.get("status"));
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id) {
        adminService.resetTeacherPassword(id);
        return Result.success();
    }

    @GetMapping("/{id}/courses")
    public Result<List<Course>> getTeacherCourses(@PathVariable Long id) {
        return Result.success(adminService.getTeacherCourses(id));
    }

    @PutMapping("/{id}/courses")
    public Result<?> assignCourses(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        adminService.assignCoursesToTeacher(id, body.get("courseIds"));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        adminService.deleteTeacher(id);
        return Result.success();
    }
}
