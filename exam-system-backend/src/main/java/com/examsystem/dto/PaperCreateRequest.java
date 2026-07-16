package com.examsystem.dto;

import lombok.Data;
import java.util.List;

@Data
public class PaperCreateRequest {
    private String paperName;
    private Long courseId;
    private Integer duration;
    private Integer totalScore;
    private List<PaperQuestionItem> questions;

    @Data
    public static class PaperQuestionItem {
        private Long questionId;
        private Integer score;
        private Integer sortOrder;
    }
}
