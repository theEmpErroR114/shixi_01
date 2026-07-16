package com.examsystem.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExamSubmitRequest {
    private List<ExamAnswerItem> answers;

    @Data
    public static class ExamAnswerItem {
        private Long questionId;
        private String studentAnswer;
    }
}
