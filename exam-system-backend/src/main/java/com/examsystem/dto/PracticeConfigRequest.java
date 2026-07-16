package com.examsystem.dto;

import lombok.Data;

@Data
public class PracticeConfigRequest {
    private Long courseId;
    private Integer questionType;
    private Integer difficulty;
    private Integer count;
}
