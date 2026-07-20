package com.examsystem.controller.teacher;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.QuestionDTO;
import com.examsystem.dto.Result;
import com.examsystem.entity.Question;
import com.examsystem.exception.BusinessException;
import com.examsystem.mapper.TeacherCourseMapper;
import com.examsystem.service.QuestionService;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/questions")
public class TeacherQuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private TeacherCourseMapper teacherCourseMapper;

    @GetMapping
    public Result<PageResult<Question>> list(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpSession session) {
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        // 如果没传 courseId，只查教师关联课程下的题目
        List<Long> courseIds = null;
        if (courseId == null) {
            courseIds = teacherCourseMapper.selectCourseIdsByTeacherId(teacherId);
            if (courseIds.isEmpty()) {
                return Result.success(PageResult.of(0L, page, pageSize, java.util.Collections.emptyList()));
            }
        }
        return Result.success(questionService.listQuestions(courseId, questionType, difficulty, keyword, page, pageSize, courseIds));
    }

    @GetMapping("/{id}")
    public Result<Question> getById(@PathVariable Long id) {
        return Result.success(questionService.getQuestionDetail(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody QuestionDTO dto, HttpSession session) {
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        // 校验课程是否在教师关联课程中
        List<Long> allowedCourseIds = teacherCourseMapper.selectCourseIdsByTeacherId(teacherId);
        if (!allowedCourseIds.contains(dto.getCourseId())) {
            throw new BusinessException("您没有该课程的权限");
        }
        questionService.createQuestion(dto, teacherId);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody QuestionDTO dto) {
        questionService.updateQuestion(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success();
    }
}
