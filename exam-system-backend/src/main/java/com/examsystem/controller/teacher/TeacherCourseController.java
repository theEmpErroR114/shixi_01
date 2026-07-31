package com.examsystem.controller.teacher;

import com.examsystem.dto.Result;
import com.examsystem.entity.Course;
import com.examsystem.mapper.TeacherCourseMapper;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 教师课程控制器 — 查询当前登录教师所授课程列表。
 * <p>
 * 教师只能看到自己被分配（通过 t_teacher_course 表）的课程。
 * 课程列表用于教师前端页面的课程下拉筛选和题目/试卷创建时的课程选择。
 *
 * @see com.examsystem.mapper.TeacherCourseMapper 教师-课程多对多关联数据访问
 */
@RestController
@RequestMapping("/api/teacher/courses")
public class TeacherCourseController {

    @Autowired
    private TeacherCourseMapper teacherCourseMapper;

    /**
     * 获取当前登录教师所授课程列表。
     * <p>
     * 从 session 中获取当前教师 ID，查询 t_teacher_course 关联表返回该教师分配的所有课程。
     *
     * @param session HTTP Session
     * @return 教师分配的课程列表（非分页，全部返回）
     */
    @GetMapping
    public Result<List<Course>> list(HttpSession session) {
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(teacherCourseMapper.selectCoursesByTeacherId(teacherId));
    }
}
