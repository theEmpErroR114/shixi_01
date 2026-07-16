package com.examsystem.entity;

import lombok.Data;

@Data
public class PaperQuestion {
    private Long id;
    private Long paperId;
    private Long questionId;
    private Integer score;
    private Integer sortOrder;

    // 非数据库字段，用于关联查询展示
    private Question question;
}
