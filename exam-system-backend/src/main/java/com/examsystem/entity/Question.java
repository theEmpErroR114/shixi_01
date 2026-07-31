package com.examsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目实体类，对应 t_question 表
 * 题目归属于课程，由教师创建
 */
@Data
public class Question {
    /** 题目ID（主键，自增） */
    private Long questionId;
    /** 所属课程ID */
    private Long courseId;
    /** 题目类型：1=单选, 2=多选, 3=判断, 4=填空, 5=简答 */
    private Integer questionType;
    /** 题目内容（题干） */
    private String content;
    /** 正确答案：单选为"A"，多选为"A,C"，判断为"对"/"错"，填空和简答为文本 */
    private String answer;
    /** 题目解析（答题后的讲解说明） */
    private String analysis;
    /** 难度：1=易, 2=中, 3=难 */
    private Integer difficulty;
    /** 创建教师ID（显示用，无外键约束，仅记录创建者） */
    private Long teacherId;
    /** 创建时间 */
    private LocalDateTime createTime;

    // ========== 以下为非数据库字段，用于关联查询展示 ==========

    /** 题目选项列表（关联查询填充，判断和填空等类型可能为空） */
    private List<QuestionOption> options;
    /** 课程名称（关联查询填充） */
    private String courseName;
    /** 教师姓名（关联查询填充） */
    private String teacherName;
}
