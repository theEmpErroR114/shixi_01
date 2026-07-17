package com.examsystem.entity;

import lombok.Data;

@Data
public class TeacherCourse {
    private Long id;
    private Long teacherId;
    private Long courseId;
}
