package com.examsystem.mapper;

import com.examsystem.entity.Course;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学生课程关联 Mapper 接口
 * 定义对 t_student_course 表的数据库操作
 * 该表维护学生与课程的多对多选课关系
 */
public interface StudentCourseMapper {

    /**
     * 查询学生已选课程列表（返回完整课程信息）
     * @param studentId 学生ID
     * @return 课程列表
     */
    List<Course> selectCoursesByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询学生已选课程的ID列表
     * @param studentId 学生ID
     * @return 课程ID列表
     */
    List<Long> selectCourseIdsByStudentId(@Param("studentId") Long studentId);

    /**
     * 删除学生的所有课程关联（删除学生时清理选课记录）
     * @param studentId 学生ID
     * @return 受影响的行数
     */
    int deleteByStudentId(@Param("studentId") Long studentId);

    /**
     * 根据课程ID删除关联（删除课程时清理所有学生的选课记录）
     * @param courseId 课程ID
     * @return 受影响的行数
     */
    int deleteByCourseId(@Param("courseId") Long courseId);

    /**
     * 为学生添加课程关联（选课）
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @return 受影响的行数
     */
    int insert(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
