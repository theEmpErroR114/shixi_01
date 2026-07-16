package com.examsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Question {
    private Long questionId;
    private Long courseId;
    private Integer questionType;
    private String content;
    private String answer;
    private String analysis;
    private Integer difficulty;
    private Long teacherId;
    private LocalDateTime createTime;

    // 非数据库字段，用于关联查询展示
    private List<QuestionOption> options;
    private String courseName;
    private String teacherName;
}
