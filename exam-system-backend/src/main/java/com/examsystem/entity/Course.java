package com.examsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课程实体类，对应 t_course 表
 * 课程是系统的核心实体，题目和试卷都归属于课程
 */
@Data
public class Course {
    /** 课程ID（主键，自增） */
    private Long courseId;
    /** 课程名称 */
    private String courseName;
    /** 课程描述 */
    private String description;
    /** 创建时间 */
    private LocalDateTime createTime;
}
