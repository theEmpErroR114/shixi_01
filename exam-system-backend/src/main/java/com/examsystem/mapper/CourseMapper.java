package com.examsystem.mapper;

import com.examsystem.entity.Course;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程 Mapper 接口
 * 定义对 t_course 表的数据库操作
 */
public interface CourseMapper {

    /**
     * 查询所有课程
     * @return 课程列表
     */
    List<Course> selectAll();

    /**
     * 根据关键字分页查询课程
     * @param keyword  关键字（模糊匹配课程名称）
     * @param offset   偏移量（分页起始位置）
     * @param pageSize 每页条数
     * @return 课程列表
     */
    List<Course> findByKeyword(@Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 统计关键字匹配的课程总数
     * @param keyword 关键字
     * @return 课程总数
     */
    Long countByKeyword(@Param("keyword") String keyword);

    /**
     * 根据课程ID查询课程
     * @param courseId 课程ID
     * @return 课程实体，未找到返回 null
     */
    Course selectById(@Param("courseId") Long courseId);

    /**
     * 新增课程
     * @param course 课程实体对象
     * @return 受影响的行数
     */
    int insert(Course course);

    /**
     * 更新课程信息
     * @param course 课程实体对象
     * @return 受影响的行数
     */
    int update(Course course);

    /**
     * 根据课程ID删除课程
     * @param courseId 课程ID
     * @return 受影响的行数
     */
    int deleteById(@Param("courseId") Long courseId);
}
