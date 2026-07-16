package com.examsystem.controller.student;

import com.examsystem.dto.ExamResultVO;
import com.examsystem.dto.ExamSubmitRequest;
import com.examsystem.dto.Result;
import com.examsystem.entity.Paper;
import com.examsystem.entity.Question;
import com.examsystem.service.ExamService;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/exams")
public class StudentExamController {

    @Autowired
    private ExamService examService;

    @GetMapping("/available")
    public Result<List<Paper>> listAvailable(HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(examService.getAvailableExams(studentId));
    }

    @PostMapping("/{paperId}/start")
    public Result<java.util.Map<String, Long>> start(@PathVariable Long paperId, HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        Long examRecordId = examService.startExam(studentId, paperId);
        return Result.success(java.util.Map.of("examRecordId", examRecordId));
    }

    @GetMapping("/{examRecordId}/detail")
    public Result<List<Question>> getDetail(@PathVariable Long examRecordId, HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(examService.getExamQuestions(examRecordId, studentId));
    }

    @PostMapping("/{examRecordId}/submit")
    public Result<ExamResultVO> submit(@PathVariable Long examRecordId,
                                        @RequestBody ExamSubmitRequest request,
                                        HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(examService.submitExam(examRecordId, studentId, request));
    }

    @GetMapping("/{examRecordId}/result")
    public Result<ExamResultVO> getResult(@PathVariable Long examRecordId, HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(examService.getExamResult(examRecordId, studentId));
    }
}
