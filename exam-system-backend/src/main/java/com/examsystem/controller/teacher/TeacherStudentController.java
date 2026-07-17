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

@RestController
@RequestMapping("/api/teacher")
public class TeacherStudentController {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private TeacherCourseMapper teacherCourseMapper;

    @GetMapping("/students/stats")
    public Result<PageResult<StudentStatsVO>> listStudentStats(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpSession session) {
        int offset = (page - 1) * pageSize;
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        List<Long> courseIds = teacherCourseMapper.selectCourseIdsByTeacherId(teacherId);
        List<Map<String, Object>> rawList;
        Long total;
        if (courseIds.isEmpty()) {
            rawList = java.util.Collections.emptyList();
            total = 0L;
        } else {
            rawList = studentMapper.selectStudentStatsByCourseIds(keyword, courseIds, offset, pageSize);
            total = studentMapper.countByCourseIds(keyword, courseIds);
        }

        List<StudentStatsVO> list = new ArrayList<>();
        for (Map<String, Object> row : rawList) {
            StudentStatsVO vo = new StudentStatsVO();
            vo.setStudentId((Long) row.get("student_id"));
            vo.setRealName((String) row.get("real_name"));
            vo.setClassName((String) row.get("class_name"));
            vo.setPracticeCount(toInt(row.get("practice_count")));
            vo.setPracticeCorrectRate(toDouble(row.get("practice_correct_rate")));
            vo.setExamCount(toInt(row.get("exam_count")));
            vo.setExamAvgScore(toDouble(row.get("exam_avg_score")));
            list.add(vo);
        }
        return Result.success(PageResult.of(total, page, pageSize, list));
    }

    @GetMapping("/students/{studentId}/detail")
    public Result<StudentDetailVO> getStudentDetail(@PathVariable Long studentId) {
        StudentDetailVO vo = new StudentDetailVO();
        vo.setStudentId(studentId);

        var student = studentMapper.selectById(studentId);
        if (student != null) {
            vo.setRealName(student.getRealName());
            vo.setClassName(student.getClassName());
            vo.setGender(student.getGender());
            vo.setPhone(student.getPhone());
        }

        List<Map<String, Object>> rawStats = studentMapper.selectStudentStatsByStudentId(studentId);
        if (!rawStats.isEmpty()) {
            Map<String, Object> row = rawStats.get(0);
            vo.setPracticeCount(toInt(row.get("practice_count")));
            vo.setPracticeCorrectRate(toDouble(row.get("practice_correct_rate")));
            vo.setExamCount(toInt(row.get("exam_count")));
            vo.setExamAvgScore(toDouble(row.get("exam_avg_score")));
        }

        List<Map<String, Object>> examScores = examRecordMapper.selectExamScoresByStudentId(studentId);
        List<StudentDetailVO.ExamScoreItem> items = new ArrayList<>();
        for (Map<String, Object> row : examScores) {
            StudentDetailVO.ExamScoreItem item = new StudentDetailVO.ExamScoreItem();
            item.setPaperName((String) row.get("paper_name"));
            item.setScore(row.get("total_score") != null ? new java.math.BigDecimal(row.get("total_score").toString()) : null);
            item.setTotalScore(row.get("paper_total_score") != null ? ((Number) row.get("paper_total_score")).intValue() : null);
            item.setSubmitTime(row.get("submit_time") != null ? row.get("submit_time").toString() : null);
            items.add(item);
        }
        vo.setExamScores(items);
        return Result.success(vo);
    }

    @GetMapping("/papers/{paperId}/scores")
    public Result<List<Map<String, Object>>> getPaperScores(@PathVariable Long paperId) {
        List<Map<String, Object>> scores = examRecordMapper.selectScoresByPaperId(paperId);
        return Result.success(scores);
    }

    private Integer toInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return 0;
    }

    private Double toDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return 0.0;
    }
}
