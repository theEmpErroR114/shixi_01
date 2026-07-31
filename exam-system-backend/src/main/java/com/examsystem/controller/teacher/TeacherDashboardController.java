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

/**
 * 教师仪表盘控制器 — 提供教师首页的统计数据（KPI 卡片）。
 * <p>
 * 统计数据包括：
 * <ul>
 *   <li>题目总数（仅统计教师所授课程下的题目）</li>
 *   <li>试卷总数 / 草稿数 / 已发布数 / 进行中考试数</li>
 *   <li>学生总数（系统中的启用学生）</li>
 * </ul>
 * 所有题目和试卷的统计都通过教师关联的课程 ID 列表进行过滤，确保教师只能看到自己课程的数据。
 *
 * @see TeacherCourseMapper#selectCourseIdsByTeacherId 获取教师关联课程
 */
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

    /**
     * 获取教师仪表盘统计数据。
     * <p>
     * 首先查询当前教师关联的所有课程 ID，然后基于这些课程 ID 统计各维度数据。
     * 如果教师尚未分配任何课程，所有计数均为 0。
     *
     * @param session HTTP Session
     * @return 统计数据 VO
     */
    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats(HttpSession session) {
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        // 获取教师当前所授所有课程的 ID 列表
        List<Long> courseIds = teacherCourseMapper.selectCourseIdsByTeacherId(teacherId);

        DashboardStatsVO vo = new DashboardStatsVO();
        if (courseIds.isEmpty()) {
            // 教师未分配任何课程，所有统计归零
            vo.setQuestionCount(0L);
            vo.setPaperCount(0L);
            vo.setDraftPaperCount(0L);
            vo.setPublishedPaperCount(0L);
            vo.setActiveExamCount(0L);
        } else {
            // 基于课程 ID 列表统计各维度数据
            vo.setQuestionCount(questionMapper.countByFiltersAndCourseIds(null, null, null, null, courseIds));
            vo.setPaperCount(paperMapper.countByFiltersAndCourseIds(null, null, courseIds));
            vo.setDraftPaperCount(paperMapper.countByFiltersAndCourseIds(0, null, courseIds));       // status=0 草稿
            vo.setPublishedPaperCount(paperMapper.countByFiltersAndCourseIds(1, null, courseIds));   // status=1 已发布
            vo.setActiveExamCount(paperMapper.countByFiltersAndCourseIds(1, null, courseIds));       // 进行中考试
        }
        vo.setStudentCount(studentMapper.countByKeyword(null, null, 1)); // 统计所有启用学生
        return Result.success(vo);
    }
}
