package com.examsystem.entity;

import lombok.Data;

@Data
public class QuestionOption {
    private Long optionId;
    private Long questionId;
    private String optionLabel;
    private String optionContent;
    private Integer isCorrect;
}
