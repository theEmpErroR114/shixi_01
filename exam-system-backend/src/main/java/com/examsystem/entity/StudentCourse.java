package com.examsystem.entity;

import lombok.Data;

/**
 * 学生课程关联实体类，对应 t_student_course 表
 * 维护学生选课的多对多关系
 */
@Data
public class StudentCourse {
    /** 关联ID（主键，自增） */
    private Long id;
    /** 学生ID */
    private Long studentId;
    /** 课程ID */
    private Long courseId;
}
