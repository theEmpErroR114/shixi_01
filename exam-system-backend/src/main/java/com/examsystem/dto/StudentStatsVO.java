package com.examsystem.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 学生统计摘要 VO
 * 用于列表页展示学生的练习和考试统计概况
 */
@Data
public class StudentStatsVO {
    /** 学生ID */
    private Long studentId;
    /** 真实姓名 */
    private String realName;
    /** 班级名称 */
    private String className;
    /** 练习总次数 */
    private Integer practiceCount;
    /** 练习正确率（0~1） */
    private Double practiceCorrectRate;
    /** 考试总次数 */
    private Integer examCount;
    /** 考试平均分 */
    private Double examAvgScore;
}
