package com.examsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 练习记录实体类，对应 t_practice_record 表
 * 记录学生每次练习的答题情况，用于错题回顾和学习分析
 */
@Data
public class PracticeRecord {
    /** 记录ID（主键，自增） */
    private Long recordId;
    /** 学生ID */
    private Long studentId;
    /** 题目ID */
    private Long questionId;
    /** 学生提交的答案内容 */
    private String studentAnswer;
    /** 是否正确：1=正确, 0=错误（选择题和判断题自动判定，填空和简答为null） */
    private Integer isCorrect;
    /** 练习时间 */
    private LocalDateTime practiceTime;

    // ========== 以下为非数据库字段，用于关联查询展示 ==========

    /** 题目内容（关联查询填充） */
    private String questionContent;
    /** 正确答案（关联查询填充） */
    private String correctAnswer;
    /** 题目解析（关联查询填充） */
    private String analysis;
    /** 课程名称（关联查询填充） */
    private String courseName;
}
