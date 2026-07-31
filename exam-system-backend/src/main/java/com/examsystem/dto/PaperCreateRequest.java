package com.examsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷创建/编辑请求 DTO
 * 教师创建新试卷或编辑草稿试卷时使用
 */
@Data
public class PaperCreateRequest {
    /** 试卷名称 */
    private String paperName;
    /** 所属课程ID */
    private Long courseId;
    /** 考试限时（分钟） */
    private Integer duration;
    /** 试卷总分 */
    private Integer totalScore;
    /** 考试开始日期（可选，null 表示不限） */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;
    /** 考试截止日期（可选，null 表示不限） */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;
    /** 试卷中的题目列表（包含每题的分值和序号） */
    private List<PaperQuestionItem> questions;

    /**
     * 试卷题目项
     */
    @Data
    public static class PaperQuestionItem {
        /** 题目ID */
        private Long questionId;
        /** 该题在试卷中的分值 */
        private Integer score;
        /** 该题在试卷中的排序序号 */
        private Integer sortOrder;
    }
}
