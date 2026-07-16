package com.examsystem.controller.teacher;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.QuestionDTO;
import com.examsystem.dto.Result;
import com.examsystem.entity.Question;
import com.examsystem.service.QuestionService;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/questions")
public class TeacherQuestionController {

    @Autowired
    private QuestionService questionService;

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
        return Result.success(questionService.listQuestions(teacherId, courseId, questionType, difficulty, keyword, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Question> getById(@PathVariable Long id) {
        return Result.success(questionService.getQuestionDetail(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody QuestionDTO dto, HttpSession session) {
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
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
