package com.examsystem.entity;

import lombok.Data;

@Data
public class StudentCourse {
    private Long id;
    private Long studentId;
    private Long courseId;
}
