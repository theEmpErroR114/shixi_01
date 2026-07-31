package com.examsystem.controller.teacher;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.Result;
import com.examsystem.dto.StudentDetailVO;
import com.examsystem.dto.StudentStatsVO;
import com.examsystem.mapper.ExamRecordMapper;
import com.examsystem.mapper.StudentMapper;
import com.examsystem.mapper.TeacherCourseMapper;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 教师学生管理控制器 — 查看学生统计数据和考试详情。
 * <p>
 * 教师可以查看自己课程下所选学生的练习和考试统计数据，以及单个学生的详细信息和成绩。
 * 所有查询都经过教师关联课程过滤（t_teacher_course JOIN t_student_course），
 * 确保教师只能看到自己课程下学生的数据。
 * <p>
 * 注意：此控制器路径前缀为 {@code /api/teacher}（非 {@code /api/teacher/students}），
 * 与其它教师控制器路径模式不同，是因为它包含了 {@code /students/**} 和 {@code /papers/**} 两种子路径。
 *
 * @see com.examsystem.mapper.StudentMapper 学生数据访问
 * @see com.examsystem.mapper.ExamRecordMapper 考试记录数据访问
 */
@RestController
@RequestMapping("/api/teacher")
public class TeacherStudentController {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private TeacherCourseMapper teacherCourseMapper;

    /**
     * 分页查询学生统计列表（练习正确率、考试平均分等）。
     * <p>
     * 通过教师关联课程 JOIN 学生选课表来确定可见学生范围，
     * 然后统计每个学生的练习次数、练习正确率、考试次数、考试平均分。
     *
     * @param keyword  搜索关键词（可选，匹配学生姓名）
     * @param page     当前页码
     * @param pageSize 每页条数
     * @param session  HTTP Session
     * @return 分页学生统计结果
     */
    @GetMapping("/students/stats")
    public Result<PageResult<StudentStatsVO>> listStudentStats(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpSession session) {
        int offset = (page - 1) * pageSize; // 计算数据库偏移量
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        // 获取教师关联课程ID列表
        List<Long> courseIds = teacherCourseMapper.selectCourseIdsByTeacherId(teacherId);
        List<Map<String, Object>> rawList;
        Long total;
        if (courseIds.isEmpty()) {
            // 教师未分配课程 → 无学生数据
            rawList = java.util.Collections.emptyList();
            total = 0L;
        } else {
            // 基于课程ID查询选修了这些课的学生及其统计数据
            rawList = studentMapper.selectStudentStatsByCourseIds(keyword, courseIds, offset, pageSize);
            total = studentMapper.countByCourseIds(keyword, courseIds);
        }

        // 将数据库原始 Map 结果转换为 VO 对象
        List<StudentStatsVO> list = new ArrayList<>();
        for (Map<String, Object> row : rawList) {
            StudentStatsVO vo = new StudentStatsVO();
            vo.setStudentId((Long) row.get("student_id"));
            vo.setRealName((String) row.get("real_name"));
            vo.setClassName((String) row.get("class_name"));
            vo.setPracticeCount(toInt(row.get("practice_count")));             // 练习次数
            vo.setPracticeCorrectRate(toDouble(row.get("practice_correct_rate"))); // 练习正确率
            vo.setExamCount(toInt(row.get("exam_count")));                     // 考试次数
            vo.setExamAvgScore(toDouble(row.get("exam_avg_score")));           // 考试平均分
            list.add(vo);
        }
        return Result.success(PageResult.of(total, page, pageSize, list));
    }

    /**
     * 获取单个学生的详细信息（含考试分数列表）。
     *
     * @param studentId 学生ID
     * @return 学生详情 VO（基本信息 + 统计数据 + 历次考试成绩）
     */
    @GetMapping("/students/{studentId}/detail")
    public Result<StudentDetailVO> getStudentDetail(@PathVariable Long studentId) {
        StudentDetailVO vo = new StudentDetailVO();
        vo.setStudentId(studentId);

        // 填充学生基本信息
        var student = studentMapper.selectById(studentId);
        if (student != null) {
            vo.setRealName(student.getRealName());
            vo.setClassName(student.getClassName());
            vo.setGender(student.getGender());
            vo.setPhone(student.getPhone());
        }

        // 填充学生统计数据（练习次数、正确率、考试次数、平均分）
        List<Map<String, Object>> rawStats = studentMapper.selectStudentStatsByStudentId(studentId);
        if (!rawStats.isEmpty()) {
            Map<String, Object> row = rawStats.get(0);
            vo.setPracticeCount(toInt(row.get("practice_count")));
            vo.setPracticeCorrectRate(toDouble(row.get("practice_correct_rate")));
            vo.setExamCount(toInt(row.get("exam_count")));
            vo.setExamAvgScore(toDouble(row.get("exam_avg_score")));
        }

        // 填充学生历次考试分数列表
        List<Map<String, Object>> examScores = examRecordMapper.selectExamScoresByStudentId(studentId);
        List<StudentDetailVO.ExamScoreItem> items = new ArrayList<>();
        for (Map<String, Object> row : examScores) {
            StudentDetailVO.ExamScoreItem item = new StudentDetailVO.ExamScoreItem();
            item.setPaperName((String) row.get("paper_name"));
            // total_score 是学生得分，可能为 null（未提交），使用 BigDecimal 保证精度
            item.setScore(row.get("total_score") != null ? new java.math.BigDecimal(row.get("total_score").toString()) : null);
            // paper_total_score 是试卷满分
            item.setTotalScore(row.get("paper_total_score") != null ? ((Number) row.get("paper_total_score")).intValue() : null);
            item.setSubmitTime(row.get("submit_time") != null ? row.get("submit_time").toString() : null);
            items.add(item);
        }
        vo.setExamScores(items);
        return Result.success(vo);
    }

    /**
     * 获取某试卷的所有学生成绩列表。
     *
     * @param paperId 试卷ID
     * @return 成绩列表（每项含学生信息和得分）
     */
    @GetMapping("/papers/{paperId}/scores")
    public Result<List<Map<String, Object>>> getPaperScores(@PathVariable Long paperId) {
        List<Map<String, Object>> scores = examRecordMapper.selectScoresByPaperId(paperId);
        return Result.success(scores);
    }

    /**
     * 安全地将数据库查询结果转为 Integer。
     * <p>
     * MyBatis 返回的聚合函数结果可能是 Long、BigDecimal、BigInteger 等类型，
     * 此方法统一转为 int，null 或非数字类型时返回 0。
     *
     * @param obj 数据库字段值
     * @return int 值，null 或非数字时返回 0
     */
    private Integer toInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return 0;
    }

    /**
     * 安全地将数据库查询结果转为 Double。
     * <p>
     * 用于处理正确率、平均分等可能为 null 或各种 Number 子类的浮点值。
     *
     * @param obj 数据库字段值
     * @return double 值，null 或非数字时返回 0.0
     */
    private Double toDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return 0.0;
    }
}
