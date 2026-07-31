package com.examsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 考试结果 VO
 * 学生提交考试后的成绩详情和每道题的作答分析
 */
@Data
public class ExamResultVO {
    /** 考试记录ID */
    private Long examRecordId;
    /** 试卷名称 */
    private String paperName;
    /** 课程名称 */
    private String courseName;
    /** 学生总分 */
    private BigDecimal totalScore;
    /** 试卷满分 */
    private Integer paperTotalScore;
    /** 考试用时（分钟） */
    private Integer usedMinutes;
    /** 排名 */
    private Long rank;
    /** 题目总数 */
    private Integer questionCount;
    /** 答对题数 */
    private Integer correctCount;
    /** 每道题的作答详情列表 */
    private List<ExamAnswerDetail> answers;

    /**
     * 考试答案详情内部类
     */
    @Data
    public static class ExamAnswerDetail {
        /** 题目ID */
        private Long questionId;
        /** 题目内容（题干） */
        private String content;
        /** 题目类型：1=单选, 2=多选, 3=判断, 4=填空, 5=简答 */
        private Integer questionType;
        /** 学生提交的答案 */
        private String studentAnswer;
        /** 正确答案 */
        private String correctAnswer;
        /** 题目解析 */
        private String analysis;
        /** 是否正确：1=正确, 0=错误 */
        private Integer isCorrect;
        /** 该题得分 */
        private BigDecimal score;
        /** 题目选项列表（选择题有选项，填空简答可能为空） */
        private List<PracticeResultVO.OptionVO> options;
    }
}
