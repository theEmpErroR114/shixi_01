package com.examsystem.controller.teacher;

import com.examsystem.dto.DashboardStatsVO;
import com.examsystem.dto.Result;
import com.examsystem.mapper.*;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/dashboard")
public class TeacherDashboardController {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherCourseMapper teacherCourseMapper;

    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats(HttpSession session) {
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        List<Long> courseIds = teacherCourseMapper.selectCourseIdsByTeacherId(teacherId);

        DashboardStatsVO vo = new DashboardStatsVO();
        if (courseIds.isEmpty()) {
            vo.setQuestionCount(0L);
            vo.setPaperCount(0L);
            vo.setDraftPaperCount(0L);
            vo.setPublishedPaperCount(0L);
            vo.setActiveExamCount(0L);
        } else {
            vo.setQuestionCount(questionMapper.countByFiltersAndCourseIds(null, null, null, null, courseIds));
            vo.setPaperCount(paperMapper.countByFiltersAndCourseIds(null, null, courseIds));
            vo.setDraftPaperCount(paperMapper.countByFiltersAndCourseIds(0, null, courseIds));
            vo.setPublishedPaperCount(paperMapper.countByFiltersAndCourseIds(1, null, courseIds));
            vo.setActiveExamCount(paperMapper.countByFiltersAndCourseIds(1, null, courseIds));
        }
        vo.setStudentCount(studentMapper.countByKeyword(null, null, 1));
        return Result.success(vo);
    }
}
