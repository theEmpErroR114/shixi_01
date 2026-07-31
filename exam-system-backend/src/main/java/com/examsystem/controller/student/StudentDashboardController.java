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

/**
 * 学生仪表盘控制器 — 提供学生首页的统计数据、即将到来的考试、最近练习记录。
 * <p>
 * 学生首页（student_dashboard.html）展示：
 * <ul>
 *   <li>统计卡片：练习总次数、考试总次数</li>
 *   <li>即将到来的考试列表（已发布且开始日期在未来）</li>
 *   <li>最近 5 条练习记录</li>
 * </ul>
 *
 * @see com.examsystem.service.PracticeService 练习业务逻辑
 * @see com.examsystem.service.ExamService 考试业务逻辑
 */
@RestController
@RequestMapping("/api/student/dashboard")
public class StudentDashboardController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private ExamService examService;

    @Autowired
    private PracticeRecordMapper practiceRecordMapper;

    /**
     * 获取学生仪表盘统计数据。
     *
     * @param session HTTP Session
     * @return Map 包含 practiceCount（练习次数）和 examCount（考试次数）
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        Long practiceCount = practiceService.countPracticeHistory(studentId, null); // null=不限课程
        Long examCount = examService.countExamHistory(studentId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("practiceCount", practiceCount);
        stats.put("examCount", examCount);
        return Result.success(stats);
    }

    /**
     * 获取即将到来的考试列表。
     * <p>
     * 考试条件：状态为已发布(status=1)，开始日期(start_date)不为空且大于当前时间，
     * 并且试卷所属课程在学生的选课列表中。
     *
     * @param session HTTP Session
     * @return 即将到来的试卷列表
     */
    @GetMapping("/upcoming-exams")
    public Result<List<Paper>> upcomingExams(HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(examService.getUpcomingExams(studentId));
    }

    /**
     * 获取最近练习记录（最多 5 条）。
     *
     * @param session HTTP Session
     * @return 最近 5 条练习记录
     */
    @GetMapping("/recent-practice")
    public Result<?> recentPractice(HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        // 固定取第1页、每页5条（不限制课程）
        return Result.success(practiceService.getPracticeHistory(studentId, null, 1, 5));
    }
}
