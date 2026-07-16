package com.examsystem.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StudentStatsVO {
    private Long studentId;
    private String realName;
    private String className;
    private Integer practiceCount;
    private Double practiceCorrectRate;
    private Integer examCount;
    private Double examAvgScore;
}
