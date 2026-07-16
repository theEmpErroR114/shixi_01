package com.examsystem.controller;

import com.examsystem.dto.Result;
import com.examsystem.entity.Course;
import com.examsystem.mapper.CourseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseMapper courseMapper;

    @GetMapping
    public Result<List<Course>> listCourses() {
        return Result.success(courseMapper.selectAll());
    }
}
