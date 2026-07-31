package com.examsystem.dto;

import lombok.Data;
import java.util.List;

/**
 * 考试提交请求 DTO
 * 学生点击交卷时前端的请求体
 */
@Data
public class ExamSubmitRequest {
    /** 所有题目的答案列表 */
    private List<ExamAnswerItem> answers;

    /**
     * 单道题目的答案项
     */
    @Data
    public static class ExamAnswerItem {
        /** 题目ID */
        private Long questionId;
        /** 学生提交的答案：
         * 单选用选项标签如"A"，
         * 多选用逗号分隔如"A,C"，
         * 判断题用"对"/"错"，
         * 填空和简答为文本 */
        private String studentAnswer;
    }
}
