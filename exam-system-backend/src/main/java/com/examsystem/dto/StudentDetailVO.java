package com.examsystem.dto;

import lombok.Data;

/**
 * 学生详情 VO
 * 包含学生基本信息和练习/考试统计数据，以及历次考试成绩明细
 */
@Data
public class StudentDetailVO {
    /** 学生ID */
    private Long studentId;
    /** 真实姓名 */
    private String realName;
    /** 班级名称 */
    private String className;
    /** 性别：M=男, F=女 */
    private String gender;
    /** 联系电话 */
    private String phone;
    /** 练习总次数 */
    private Integer practiceCount;
    /** 练习正确率（0~1 保留两位小数，如 0.85 表示 85%） */
    private Double practiceCorrectRate;
    /** 考试总次数 */
    private Integer examCount;
    /** 考试平均分 */
    private Double examAvgScore;
    /** 历次考试成绩列表 */
    private java.util.List<ExamScoreItem> examScores;

    /**
     * 考试成绩项
     */
    @Data
    public static class ExamScoreItem {
        /** 试卷名称 */
        private String paperName;
        /** 得分 */
        private java.math.BigDecimal score;
        /** 试卷总分 */
        private Integer totalScore;
        /** 交卷时间 */
        private String submitTime;
    }
}
