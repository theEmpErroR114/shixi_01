package com.examsystem.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamRecord {
    private Long examRecordId;
    private Long studentId;
    private Long paperId;
    private BigDecimal totalScore;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer status;

    // 非数据库字段
    private String paperName;
    private String courseName;
    private Integer paperDuration;
    private Integer paperTotalScore;
    private String studentName;
    private String className;
    private List<ExamAnswer> answers;
}
