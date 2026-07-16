package com.examsystem.controller.student;

import com.examsystem.dto.ExamResultVO;
import com.examsystem.dto.PageResult;
import com.examsystem.dto.Result;
import com.examsystem.entity.ExamRecord;
import com.examsystem.service.ExamService;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/results")
public class StudentResultController {

    @Autowired
    private ExamService examService;

    @GetMapping("/exams")
    public Result<PageResult<ExamRecord>> listExams(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        List<ExamRecord> list = examService.getExamHistory(studentId, page, pageSize);
        Long total = examService.countExamHistory(studentId);
        return Result.success(PageResult.of(total, page, pageSize, list));
    }

    @GetMapping("/exams/{examRecordId}")
    public Result<ExamResultVO> getExamResult(@PathVariable Long examRecordId, HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(examService.getExamResult(examRecordId, studentId));
    }
}
