package com.examsystem.dto;

import lombok.Data;

@Data
public class PracticeSubmitRequest {
    private Long questionId;
    private String studentAnswer;
}
