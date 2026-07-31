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

/**
 * 学生考试控制器 — 提供考试全流程操作：查看可用考试、开始考试、获取题目、提交答案、查看结果。
 * <p>
 * 考试流程：
 * <ol>
 *   <li>GET {@code /available} — 获取当前可参加的考试列表（已发布且在有效期内）</li>
 *   <li>POST {@code /{paperId}/start} — 开始考试，创建考试记录并返回 examRecordId</li>
 *   <li>GET {@code /{examRecordId}/detail} — 获取考试题目列表（不含答案）</li>
 *   <li>POST {@code /{examRecordId}/submit} — 提交考试答案，触发自动评分</li>
 *   <li>GET {@code /{examRecordId}/result} — 查看考试成绩和详情</li>
 * </ol>
 * <p>
 * <b>重要：</b>题目中不含正确答案（answer 和 analysis 字段为 null），
 * 只有在提交后查看结果时才会返回正确答案和解析。
 *
 * @see com.examsystem.service.ExamService 考试业务逻辑
 */
@RestController
@RequestMapping("/api/student/exams")
public class StudentExamController {

    @Autowired
    private ExamService examService;

    /**
     * 获取当前学生可参加的考试列表。
     * <p>
     * 条件：试卷状态为已发布(1)，且当前时间在 start_date 和 end_date 范围内（如设置了日期），
     * 且试卷所属课程在学生选课列表中。
     *
     * @param session HTTP Session
     * @return 可用考试列表
     */
    @GetMapping("/available")
    public Result<List<Paper>> listAvailable(HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(examService.getAvailableExams(studentId));
    }

    /**
     * 开始考试。
     * <p>
     * 创建一条考试记录（t_exam_record），状态标记为"进行中"。
     * 如果学生已经创建过该试卷的考试记录，后续请求会返回已有记录（防止重复创建）。
     *
     * @param paperId 试卷ID
     * @param session HTTP Session
     * @return 包含 examRecordId 的 Map，后续操作需要使用此 ID
     */
    @PostMapping("/{paperId}/start")
    public Result<java.util.Map<String, Long>> start(@PathVariable Long paperId, HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        Long examRecordId = examService.startExam(studentId, paperId);
        return Result.success(java.util.Map.of("examRecordId", examRecordId));
    }

    /**
     * 获取考试题目详情（考试中查看，不含答案）。
     * <p>
     * 返回题目列表，其中 answer 和 analysis 字段为 null，防止学生作弊。
     * 同时会校验考试记录是否属于当前学生，防止越权访问。
     *
     * @param examRecordId 考试记录ID（由 start 接口返回）
     * @param session      HTTP Session
     * @return 题目列表（不含正确答案）
     */
    @GetMapping("/{examRecordId}/detail")
    public Result<List<Question>> getDetail(@PathVariable Long examRecordId, HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(examService.getExamQuestions(examRecordId, studentId));
    }

    /**
     * 提交考试答案。
     * <p>
     * 将学生答案保存到数据库，触发自动评分逻辑：
     * 单选和判断题通过精确匹配评分，多选题通过排序后比较，填空和简答题返回参考答案供人工评阅。
     *
     * @param examRecordId 考试记录ID
     * @param request      提交请求体（含答案列表）
     * @param session      HTTP Session
     * @return 考试结果 VO（含总分、每道题的判分结果）
     */
    @PostMapping("/{examRecordId}/submit")
    public Result<ExamResultVO> submit(@PathVariable Long examRecordId,
                                        @RequestBody ExamSubmitRequest request,
                                        HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(examService.submitExam(examRecordId, studentId, request));
    }

    /**
     * 查看考试结果（已提交后可查看，含正确答案和解析）。
     * <p>
     * 如果考试未提交，调用此接口会返回考试成绩为空的记录。
     *
     * @param examRecordId 考试记录ID
     * @param session      HTTP Session
     * @return 考试结果 VO（含总分、每道题的正确答案、解析、学生答案）
     */
    @GetMapping("/{examRecordId}/result")
    public Result<ExamResultVO> getResult(@PathVariable Long examRecordId, HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(examService.getExamResult(examRecordId, studentId));
    }
}
