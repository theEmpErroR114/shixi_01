package com.examsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ExamResultVO {
    private Long examRecordId;
    private String paperName;
    private String courseName;
    private BigDecimal totalScore;
    private Integer paperTotalScore;
    private Integer usedMinutes;
    private Long rank;
    private Integer questionCount;
    private Integer correctCount;
    private List<ExamAnswerDetail> answers;

    @Data
    public static class ExamAnswerDetail {
        private Long questionId;
        private String content;
        private Integer questionType;
        private String studentAnswer;
        private String correctAnswer;
        private String analysis;
        private Integer isCorrect;
        private BigDecimal score;
        private List<PracticeResultVO.OptionVO> options;
    }
}
