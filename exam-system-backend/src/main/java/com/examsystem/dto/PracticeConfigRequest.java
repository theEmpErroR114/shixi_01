package com.examsystem.dto;

import lombok.Data;

/**
 * 练习配置请求 DTO
 * 学生开始练习前，设置练习的筛选条件
 */
@Data
public class PracticeConfigRequest {
    /** 课程ID */
    private Long courseId;
    /** 题目类型（null 表示不限） */
    private Integer questionType;
    /** 难度（null 表示不限） */
    private Integer difficulty;
    /** 练习题目数量 */
    private Integer count;
}
