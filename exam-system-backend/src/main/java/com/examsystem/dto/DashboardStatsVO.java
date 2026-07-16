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
    private Long paperCount;
    private Long publishedPaperCount;
}
