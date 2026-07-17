package com.examsystem.controller.teacher;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.PaperCreateRequest;
import com.examsystem.dto.Result;
import com.examsystem.entity.Paper;
import com.examsystem.exception.BusinessException;
import com.examsystem.mapper.TeacherCourseMapper;
import com.examsystem.service.PaperService;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/papers")
public class TeacherPaperController {

    @Autowired
    private PaperService paperService;

    @Autowired
    private TeacherCourseMapper teacherCourseMapper;

    @GetMapping
    public Result<PageResult<Paper>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpSession session) {
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(paperService.listPapers(teacherId, status, courseId, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Paper> getById(@PathVariable Long id) {
        return Result.success(paperService.getPaperDetail(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody PaperCreateRequest request, HttpSession session) {
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        List<Long> allowedCourseIds = teacherCourseMapper.selectCourseIdsByTeacherId(teacherId);
        if (!allowedCourseIds.contains(request.getCourseId())) {
            throw new BusinessException("您没有该课程的权限");
        }
        paperService.createPaper(request, teacherId);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody PaperCreateRequest request) {
        paperService.updatePaper(id, request);
        return Result.success();
    }

    @PutMapping("/{id}/publish")
    public Result<?> publish(@PathVariable Long id) {
        paperService.publishPaper(id);
        return Result.success();
    }

    @PutMapping("/{id}/recall")
    public Result<?> recall(@PathVariable Long id) {
        paperService.recallPaper(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        paperService.deletePaper(id);
        return Result.success();
    }
}
