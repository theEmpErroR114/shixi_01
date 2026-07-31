package com.examsystem.dto;

import lombok.Data;

/**
 * 仪表盘统计信息 VO
 * 管理员和教师仪表盘共用此数据结构，不同角色使用不同字段子集
 */
@Data
public class DashboardStatsVO {
    // ========== 管理员仪表盘字段 ==========

    /** 教师总数 */
    private Long teacherCount;
    /** 启用的教师数量 */
    private Long activeTeacherCount;
    /** 学生总数 */
    private Long studentCount;
    /** 启用的学生数量 */
    private Long activeStudentCount;

    // ========== 教师仪表盘字段 ==========

    /** 题目总数 */
    private Long questionCount;
    /** 本月新增题目数 */
    private Long newQuestionsThisMonth;
    /** 试卷总数 */
    private Long paperCount;
    /** 草稿试卷数 */
    private Long draftPaperCount;
    /** 已发布试卷数 */
    private Long publishedPaperCount;
    /** 进行中的考试数 */
    private Long activeExamCount;
    /** 班级数量 */
    private Long classCount;
}
