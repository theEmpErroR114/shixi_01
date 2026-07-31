package com.examsystem.entity;

import lombok.Data;

/**
 * 教师课程关联实体类，对应 t_teacher_course 表
 * 维护教师授课的多对多关系
 */
@Data
public class TeacherCourse {
    /** 关联ID（主键，自增） */
    private Long id;
    /** 教师ID */
    private Long teacherId;
    /** 课程ID */
    private Long courseId;
}
