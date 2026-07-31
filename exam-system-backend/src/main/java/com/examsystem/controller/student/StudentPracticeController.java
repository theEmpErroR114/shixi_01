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

/**
 * 学生练习控制器 — 提供练习生成、答案提交和练习历史查询。
 * <p>
 * 练习流程：
 * <ol>
 *   <li>POST {@code /generate} — 根据配置（课程、题型、难度、数量）生成练习题列表</li>
 *   <li>POST {@code /submit} — 逐题提交答案，返回即时反馈（正确/错误、解析）</li>
 *   <li>GET {@code /history} — 分页查询练习历史记录</li>
 * </ol>
 * <p>
 * 与考试不同，练习采用逐题提交模式（确认答案 → 查看反馈 → 下一题），
 * 而非一次提交全部答案。练习题不包含正确答案（answer 和 analysis 为 null），
 * 只有在提交后才会返回正确答案和解析。
 *
 * @see com.examsystem.service.PracticeService 练习业务逻辑
 */
@RestController
@RequestMapping("/api/student/practice")
public class StudentPracticeController {

    @Autowired
    private PracticeService practiceService;

    /**
     * 根据配置生成练习题列表。
     * <p>
     * 题目来源为学生在所选课程下已选课程的题目池。
     * 生成的题目不包含正确答案——answer 和 analysis 字段设为 null。
     * 判断题（type=3）在数据库中没有选项时会自动生成"对"/"错"两个默认选项。
     *
     * @param request 练习配置（课程ID、题型筛选、难度筛选、题目数量）
     * @param session HTTP Session
     * @return 练习题列表（不含答案）
     */
    @PostMapping("/generate")
    public Result<List<Question>> generate(@RequestBody PracticeConfigRequest request, HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(practiceService.generatePractice(studentId, request));
    }

    /**
     * 提交单道练习题的答案。
     * <p>
     * 提交后立即返回反馈：是否正确、正确答案、题目解析。
     * 同时将练习记录写入 t_practice_record 表。
     * <p>
     * 评分规则：
     * <ul>
     *   <li>单选/判断：精确字符串匹配</li>
     *   <li>多选：排序后比较（忽略选项顺序）</li>
     *   <li>填空/简答：返回参考答案供学生自行对比</li>
     * </ul>
     *
     * @param request 提交请求体（题目ID + 学生答案）
     * @param session HTTP Session
     * @return 练习结果 VO（含正确性、正确答案、解析）
     */
    @PostMapping("/submit")
    public Result<PracticeResultVO> submit(@RequestBody com.examsystem.dto.PracticeSubmitRequest request, HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(practiceService.submitAnswer(studentId, request.getQuestionId(), request.getStudentAnswer()));
    }

    /**
     * 分页查询练习历史记录。
     *
     * @param courseId 课程ID筛选（可选）
     * @param page     当前页码（默认第1页）
     * @param pageSize 每页条数（默认20条）
     * @param session  HTTP Session
     * @return 分页练习记录（每题一条记录，含题目文本、学生答案、是否正确）
     */
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
