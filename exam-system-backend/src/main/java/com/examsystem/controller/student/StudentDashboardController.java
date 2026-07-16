package com.examsystem.controller.student;

import com.examsystem.dto.Result;
import com.examsystem.entity.Paper;
import com.examsystem.mapper.PracticeRecordMapper;
import com.examsystem.service.ExamService;
import com.examsystem.service.PracticeService;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/dashboard")
public class StudentDashboardController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private ExamService examService;

    @Autowired
    private PracticeRecordMapper practiceRecordMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        Long practiceCount = practiceService.countPracticeHistory(studentId, null);
        Long examCount = examService.countExamHistory(studentId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("practiceCount", practiceCount);
        stats.put("examCount", examCount);
        return Result.success(stats);
    }

    @GetMapping("/upcoming-exams")
    public Result<List<Paper>> upcomingExams(HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(examService.getAvailableExams(studentId));
    }

    @GetMapping("/recent-practice")
    public Result<?> recentPractice(HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(practiceService.getPracticeHistory(studentId, null, 1, 5));
    }
}
