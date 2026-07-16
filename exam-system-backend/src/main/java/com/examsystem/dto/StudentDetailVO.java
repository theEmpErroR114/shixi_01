package com.examsystem.dto;

import lombok.Data;

@Data
public class StudentDetailVO {
    private Long studentId;
    private String realName;
    private String className;
    private String gender;
    private String phone;
    private Integer practiceCount;
    private Double practiceCorrectRate;
    private Integer examCount;
    private Double examAvgScore;
    private java.util.List<ExamScoreItem> examScores;

    @Data
    public static class ExamScoreItem {
        private String paperName;
        private java.math.BigDecimal score;
        private Integer totalScore;
        private String submitTime;
    }
}
