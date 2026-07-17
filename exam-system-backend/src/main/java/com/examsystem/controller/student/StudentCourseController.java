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

@RestController
@RequestMapping("/api/student/courses")
public class StudentCourseController {

    @Autowired
    private StudentCourseMapper studentCourseMapper;

    @GetMapping
    public Result<List<Course>> list(HttpSession session) {
        Long studentId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        return Result.success(studentCourseMapper.selectCoursesByStudentId(studentId));
    }
}
