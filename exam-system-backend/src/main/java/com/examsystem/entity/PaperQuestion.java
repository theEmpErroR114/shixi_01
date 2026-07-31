package com.examsystem.entity;

import lombok.Data;

/**
 * 试卷题目关联实体类，对应 t_paper_question 表
 * 维护试卷与题目的多对多关系，每条记录还包含题目在该试卷中的分值、序号
 */
@Data
public class PaperQuestion {
    /** 关联ID（主键，自增） */
    private Long id;
    /** 试卷ID */
    private Long paperId;
    /** 题目ID */
    private Long questionId;
    /** 该题在试卷中的分值 */
    private Integer score;
    /** 该题在试卷中的排序序号 */
    private Integer sortOrder;

    // ========== 以下为非数据库字段，用于关联查询展示 ==========

    /** 题目详情（关联查询填充，包含题目内容、选项、答案等） */
    private Question question;
}
