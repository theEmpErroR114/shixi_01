package com.examsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PracticeRecord {
    private Long recordId;
    private Long studentId;
    private Long questionId;
    private String studentAnswer;
    private Integer isCorrect;
    private LocalDateTime practiceTime;

    // 非数据库字段
    private String questionContent;
    private String correctAnswer;
    private String analysis;
    private String courseName;
}
