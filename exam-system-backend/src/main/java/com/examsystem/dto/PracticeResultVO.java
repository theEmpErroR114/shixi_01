package com.examsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 练习结果 VO
 * 学生确认答案后返回的题目批改结果
 */
@Data
public class PracticeResultVO {
    /** 题目ID */
    private Long questionId;
    /** 题目内容（题干） */
    private String content;
    /** 学生提交的答案 */
    private String studentAnswer;
    /** 正确答案 */
    private String correctAnswer;
    /** 是否正确 */
    private Boolean isCorrect;
    /** 题目解析 */
    private String analysis;
    /** 题目选项列表（选择题有选项，填空简答为空） */
    private List<OptionVO> options;

    /**
     * 选项 VO（用于展示，不含 isCorrect 字段，练习中不透露答案）
     */
    @Data
    public static class OptionVO {
        /** 选项标签（如 A、B、C、D） */
        private String optionLabel;
        /** 选项内容文本 */
        private String optionContent;
    }
}
