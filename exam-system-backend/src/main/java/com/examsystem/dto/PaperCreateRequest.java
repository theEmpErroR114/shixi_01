package com.examsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaperCreateRequest {
    private String paperName;
    private Long courseId;
    private Integer duration;
    private Integer totalScore;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;
    private List<PaperQuestionItem> questions;

    @Data
    public static class PaperQuestionItem {
        private Long questionId;
        private Integer score;
        private Integer sortOrder;
    }
}
