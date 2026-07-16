package com.examsystem.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExamAnswer {
    private Long answerId;
    private Long examRecordId;
    private Long questionId;
    private String studentAnswer;
    private Integer isCorrect;
    private BigDecimal score;

    // 非数据库字段
    private String questionContent;
    private String correctAnswer;
    private String analysis;
    private Integer questionType;
}
