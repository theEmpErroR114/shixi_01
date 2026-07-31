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

/**
 * 学生成绩结果控制器 — 提供考试历史查询和单次考试结果详情查看。
 * <p>
 * 与考试进行中的 {@link com.examsystem.controller.student.StudentExamController} 不同，
 * 此控制器专注于已完成的考试结果查看：
 * <ul>
 *   <li>历史考试列表（分页）</li>
 *   <li>单次考试结果详情（含得分、正确答案、解析）</li>
 * </ul>
 * <p>
 * 此接口对应前端 student_results.html 页面。
 *
 * @see com.examsystem.service.ExamService 考试业务逻辑
 * @see StudentExamController 考试进行中相关接口
 */
@RestController
@RequestMapping("/api/student/results")
public class StudentResultController {

    @Autowired
    private ExamService examService;

    /**
     * 分页查询学生的考试历史记录。
     *
     * @param page     当前页码（默认第1页）
     * @param pageSize 每页条数（默认10条）
     * @param session  HTTP Session
     * @return 分页考试记录（含试卷名称、得分、总分、提交时间等）
     */
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

    /**
     * 查看单次考试的详细结果。
     * <p>
     * 返回内容包括：总分、每道题的得分、学生答案、正确答案、解析。
     * 会校验考试记录是否属于当前学生，防止越权查看。
     *
     * @param examRecordId 考试记录ID
     * @param session      HTTP Session
     * @return 考试结果 VO（含详细评分信息）
     */
    @GetMapping("/exams/{examRecordId}")
    public Result<ExamResultVO> getExamResult(@PathVariable Long examRecordId, HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(examService.getExamResult(examRecordId, studentId));
    }
}
