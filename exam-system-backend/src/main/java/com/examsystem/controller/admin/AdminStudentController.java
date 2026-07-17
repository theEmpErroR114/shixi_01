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

@RestController
@RequestMapping("/api/admin/students")
public class AdminStudentController {

    @Autowired
    private AdminService adminService;

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

    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Long id) {
        return Result.success(adminService.getStudentById(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody Student student, HttpSession session) {
        Long adminId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        student.setCreateBy(adminId);
        adminService.createStudent(student);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Student student) {
        student.setStudentId(id);
        adminService.updateStudent(student);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateStudentStatus(id, body.get("status"));
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id) {
        adminService.resetStudentPassword(id);
        return Result.success();
    }

    @GetMapping("/{id}/courses")
    public Result<List<Course>> getStudentCourses(@PathVariable Long id) {
        return Result.success(adminService.getStudentCourses(id));
    }

    @PutMapping("/{id}/courses")
    public Result<?> assignCourses(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        adminService.assignCoursesToStudent(id, body.get("courseIds"));
        return Result.success();
    }
}
