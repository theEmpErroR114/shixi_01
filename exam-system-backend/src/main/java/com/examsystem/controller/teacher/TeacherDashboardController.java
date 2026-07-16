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

@RestController
@RequestMapping("/api/teacher/dashboard")
public class TeacherDashboardController {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private StudentMapper studentMapper;

    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats(HttpSession session) {
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setQuestionCount(questionMapper.countByFilters(teacherId, null, null, null, null));
        vo.setPaperCount(paperMapper.countByFilters(teacherId, null, null));
        vo.setDraftPaperCount(paperMapper.countByFilters(teacherId, 0, null));
        vo.setPublishedPaperCount(paperMapper.countByFilters(teacherId, 1, null));
        vo.setActiveExamCount(paperMapper.countByFilters(teacherId, 1, null));
        vo.setStudentCount(studentMapper.countByKeyword(null, null, 1));
        return Result.success(vo);
    }
}
