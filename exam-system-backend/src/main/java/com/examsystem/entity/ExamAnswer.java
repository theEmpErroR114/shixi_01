package com.examsystem.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 考试答案实体类，对应 t_exam_answer 表
 * 记录学生在考试中对每道题目的作答情况
 */
@Data
public class ExamAnswer {
    /** 答案ID（主键，自增） */
    private Long answerId;
    /** 所属的考试记录ID */
    private Long examRecordId;
    /** 题目ID */
    private Long questionId;
    /** 学生提交的答案内容 */
    private String studentAnswer;
    /** 是否正确：1=正确, 0=错误（选择题和判断题自动判定，填空和简答为null） */
    private Integer isCorrect;
    /** 该题得分 */
    private BigDecimal score;

    // ========== 以下为非数据库字段，用于关联查询展示 ==========

    /** 题目内容（关联查询填充） */
    private String questionContent;
    /** 正确答案（关联查询填充） */
    private String correctAnswer;
    /** 题目解析（关联查询填充） */
    private String analysis;
    /** 题目类型（关联查询填充）：1=单选, 2=多选, 3=判断, 4=填空, 5=简答 */
    private Integer questionType;
}
