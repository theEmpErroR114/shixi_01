package com.examsystem.controller;

import com.examsystem.dto.Result;
import com.examsystem.entity.Course;
import com.examsystem.mapper.CourseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程公共控制器 — 提供所有课程列表查询。
 * <p>
 * 此接口为公开接口（不需要角色权限），主要用于：
 * <ul>
 *   <li>Admin 后台管理页面获取课程列表（如下拉选择）</li>
 *   <li>其他需要课程基础信息的场景</li>
 * </ul>
 * 注意：教师和学生各自有专门的课程接口（{@code /api/teacher/courses} 和 {@code /api/student/courses}），
 * 返回的是与当前用户关联的课程子集。
 *
 * @see com.examsystem.controller.teacher.TeacherCourseController 教师课程接口
 * @see com.examsystem.controller.student.StudentCourseController 学生课程接口
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseMapper courseMapper;

    /**
     * 获取系统中所有课程列表。
     * <p>
     * 返回完整课程列表，不进行分页。适用于下拉选择等需要全部数据的场景。
     *
     * @return 所有课程列表
     */
    @GetMapping
    public Result<List<Course>> listCourses() {
        return Result.success(courseMapper.selectAll());
    }
}
