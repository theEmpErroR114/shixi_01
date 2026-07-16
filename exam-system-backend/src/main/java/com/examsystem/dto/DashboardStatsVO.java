package com.examsystem.dto;

import lombok.Data;

@Data
public class DashboardStatsVO {
    // Admin dashboard
    private Long teacherCount;
    private Long activeTeacherCount;
    private Long studentCount;
    private Long activeStudentCount;

    // Teacher dashboard
    private Long questionCount;
    private Long newQuestionsThisMonth;
    private Long paperCount;
    private Long draftPaperCount;
    private Long publishedPaperCount;
    private Long activeExamCount;
    private Long classCount;
}
