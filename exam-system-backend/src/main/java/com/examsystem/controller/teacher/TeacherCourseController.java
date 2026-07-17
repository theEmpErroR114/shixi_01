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

@RestController
@RequestMapping("/api/teacher/courses")
public class TeacherCourseController {

    @Autowired
    private TeacherCourseMapper teacherCourseMapper;

    @GetMapping
    public Result<List<Course>> list(HttpSession session) {
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(teacherCourseMapper.selectCoursesByTeacherId(teacherId));
    }
}
