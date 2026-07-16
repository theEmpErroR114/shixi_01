package com.examsystem.controller.student;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.PracticeConfigRequest;
import com.examsystem.dto.PracticeResultVO;
import com.examsystem.dto.Result;
import com.examsystem.entity.PracticeRecord;
import com.examsystem.entity.Question;
import com.examsystem.service.PracticeService;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/practice")
public class StudentPracticeController {

    @Autowired
    private PracticeService practiceService;

    @PostMapping("/generate")
    public Result<List<Question>> generate(@RequestBody PracticeConfigRequest request, HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(practiceService.generatePractice(studentId, request));
    }

    @PostMapping("/submit")
    public Result<PracticeResultVO> submit(@RequestBody com.examsystem.dto.PracticeSubmitRequest request, HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(practiceService.submitAnswer(studentId, request.getQuestionId(), request.getStudentAnswer()));
    }

    @GetMapping("/history")
    public Result<PageResult<PracticeRecord>> history(
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        List<PracticeRecord> list = practiceService.getPracticeHistory(studentId, courseId, page, pageSize);
        Long total = practiceService.countPracticeHistory(studentId, courseId);
        return Result.success(PageResult.of(total, page, pageSize, list));
    }
}
