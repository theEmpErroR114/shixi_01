package com.examsystem.entity;

import lombok.Data;

/**
 * 题目选项实体类，对应 t_question_option 表
 * 用于选择题（单选、多选）和判断题的选项存储
 */
@Data
public class QuestionOption {
    /** 选项ID（主键，自增） */
    private Long optionId;
    /** 所属题目ID */
    private Long questionId;
    /** 选项标签（如 A、B、C、D） */
    private String optionLabel;
    /** 选项内容文本 */
    private String optionContent;
    /** 是否为正确答案：1=是, 0=否 */
    private Integer isCorrect;
}
