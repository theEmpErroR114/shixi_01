package com.examsystem.controller.admin;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.Result;
import com.examsystem.entity.Course;
import com.examsystem.entity.Paper;
import com.examsystem.entity.Question;
import com.examsystem.service.AdminService;
import com.examsystem.service.PaperService;
import com.examsystem.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private PaperService paperService;

    @GetMapping
    public Result<PageResult<Course>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Course> list = adminService.listCourses(keyword, page, pageSize);
        Long total = adminService.countCourses(keyword);
        return Result.success(PageResult.of(total, page, pageSize, list));
    }

    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Long id) {
        return Result.success(adminService.getCourseById(id));
    }

    @GetMapping("/{courseId}/questions")
    public Result<PageResult<Question>> listQuestions(
            @PathVariable Long courseId,
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(questionService.listQuestions(courseId, questionType, difficulty, keyword, page, pageSize));
    }

    @GetMapping("/{courseId}/papers")
    public Result<PageResult<Paper>> listPapers(
            @PathVariable Long courseId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(paperService.listPapers(status, courseId, page, pageSize));
    }

    @PostMapping
    public Result<?> create(@RequestBody Course course) {
        adminService.createCourse(course);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Course course) {
        course.setCourseId(id);
        adminService.updateCourse(course);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        adminService.deleteCourse(id);
        return Result.success();
    }
}
