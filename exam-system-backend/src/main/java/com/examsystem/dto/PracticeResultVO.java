package com.examsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PracticeResultVO {
    private Long questionId;
    private String content;
    private String studentAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private String analysis;
    private List<OptionVO> options;

    @Data
    public static class OptionVO {
        private String optionLabel;
        private String optionContent;
    }
}
