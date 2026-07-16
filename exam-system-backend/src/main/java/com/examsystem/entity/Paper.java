package com.examsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Paper {
    private Long paperId;
    private String paperName;
    private Long courseId;
    private Long teacherId;
    private Integer totalScore;
    private Integer duration;
    private Integer status;
    private LocalDateTime createTime;

    // 非数据库字段，用于关联查询展示
    private String courseName;
    private String teacherName;
    private List<PaperQuestion> questions;
}
