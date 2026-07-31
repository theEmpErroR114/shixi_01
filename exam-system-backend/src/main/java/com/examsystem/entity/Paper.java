package com.examsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷实体类，对应 t_paper 表
 * 教师创建试卷后需"发布"学生才能参加，已发布的试卷可"回收"
 */
@Data
public class Paper {
    /** 试卷ID（主键，自增） */
    private Long paperId;
    /** 试卷名称 */
    private String paperName;
    /** 所属课程ID（试卷按课程分类） */
    private Long courseId;
    /** 创建教师ID（显示用，无外键约束，仅记录创建者） */
    private Long teacherId;
    /** 试卷总分 */
    private Integer totalScore;
    /** 考试限时（分钟） */
    private Integer duration;
    /** 试卷状态：0=未发布（草稿）, 1=已发布, 2=已回收 */
    private Integer status;
    /** 考试开始日期（可选，null 表示不限） */
    private LocalDateTime startDate;
    /** 考试截止日期（可选，null 表示不限） */
    private LocalDateTime endDate;
    /** 创建时间 */
    private LocalDateTime createTime;

    // ========== 以下为非数据库字段，用于关联查询展示 ==========

    /** 课程名称（关联查询填充） */
    private String courseName;
    /** 教师姓名（关联查询填充） */
    private String teacherName;
    /** 试卷中的题目列表（含每题分数和序号，关联查询填充） */
    private List<PaperQuestion> questions;
}
