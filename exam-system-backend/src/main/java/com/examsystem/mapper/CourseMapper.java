package com.examsystem.mapper;

import com.examsystem.entity.Course;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CourseMapper {

    List<Course> selectAll();

    List<Course> findByKeyword(@Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);

    Long countByKeyword(@Param("keyword") String keyword);

    Course selectById(@Param("courseId") Long courseId);

    int insert(Course course);

    int update(Course course);

    int deleteById(@Param("courseId") Long courseId);
}
