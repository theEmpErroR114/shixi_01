package com.examsystem.mapper;

import com.examsystem.entity.Course;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CourseMapper {

    List<Course> selectAll();

    Course selectById(@Param("courseId") Long courseId);

    int insert(Course course);
}
