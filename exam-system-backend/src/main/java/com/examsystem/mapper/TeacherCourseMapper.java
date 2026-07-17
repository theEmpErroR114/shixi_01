package com.examsystem.mapper;

import com.examsystem.entity.Course;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TeacherCourseMapper {

    List<Course> selectCoursesByTeacherId(@Param("teacherId") Long teacherId);

    List<Long> selectCourseIdsByTeacherId(@Param("teacherId") Long teacherId);

    int deleteByTeacherId(@Param("teacherId") Long teacherId);

    int insert(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);
}
