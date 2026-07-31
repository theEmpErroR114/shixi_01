package com.examsystem.mapper;

import com.examsystem.entity.Course;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 教师课程关联 Mapper 接口
 * 定义对 t_teacher_course 表的数据库操作
 * 该表维护教师与课程的多对多授课关系
 */
public interface TeacherCourseMapper {

    /**
     * 查询教师所授课程列表（返回完整课程信息）
     * @param teacherId 教师ID
     * @return 课程列表
     */
    List<Course> selectCoursesByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 查询教师所授课程的ID列表
     * @param teacherId 教师ID
     * @return 课程ID列表
     */
    List<Long> selectCourseIdsByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 删除教师的所有课程关联（删除教师时清理授课记录）
     * @param teacherId 教师ID
     * @return 受影响的行数
     */
    int deleteByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 根据课程ID删除关联（删除课程时清理所有教师的授课记录）
     * @param courseId 课程ID
     * @return 受影响的行数
     */
    int deleteByCourseId(@Param("courseId") Long courseId);

    /**
     * 为教师添加课程关联（分配课程）
     * @param teacherId 教师ID
     * @param courseId  课程ID
     * @return 受影响的行数
     */
    int insert(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);
}
