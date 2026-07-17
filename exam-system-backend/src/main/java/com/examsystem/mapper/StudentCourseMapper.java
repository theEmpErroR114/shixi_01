package com.examsystem.mapper;

import com.examsystem.entity.Course;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StudentCourseMapper {

    List<Course> selectCoursesByStudentId(@Param("studentId") Long studentId);

    List<Long> selectCourseIdsByStudentId(@Param("studentId") Long studentId);

    int deleteByStudentId(@Param("studentId") Long studentId);

    int insert(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
