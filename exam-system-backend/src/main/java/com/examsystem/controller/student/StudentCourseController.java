package com.examsystem.controller.student;

import com.examsystem.dto.Result;
import com.examsystem.entity.Course;
import com.examsystem.mapper.StudentCourseMapper;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生课程控制器 — 查询当前登录学生已选课程列表。
 * <p>
 * 学生只能看到自己通过 t_student_course 表选中的课程。
 * 课程列表用于前端页面的课程筛选和练习/考试时的课程选择。
 *
 * @see com.examsystem.mapper.StudentCourseMapper 学生-课程多对多关联数据访问
 */
@RestController
@RequestMapping("/api/student/courses")
public class StudentCourseController {

    @Autowired
    private StudentCourseMapper studentCourseMapper;

    /**
     * 获取当前登录学生已选课程列表。
     * <p>
     * 从 session 中获取当前学生 ID，查询 t_student_course 关联表返回该学生选中的所有课程。
     *
     * @param session HTTP Session
     * @return 学生选课列表（非分页，全部返回）
     */
    @GetMapping
    public Result<List<Course>> list(HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(studentCourseMapper.selectCoursesByStudentId(studentId));
    }
}
