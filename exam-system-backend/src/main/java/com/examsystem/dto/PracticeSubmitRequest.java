package com.examsystem.dto;

import lombok.Data;

/**
 * 练习提交请求 DTO
 * 学生在练习模式中确认单道题目的答案时使用
 */
@Data
public class PracticeSubmitRequest {
    /** 题目ID */
    private Long questionId;
    /** 学生提交的答案 */
    private String studentAnswer;
}
