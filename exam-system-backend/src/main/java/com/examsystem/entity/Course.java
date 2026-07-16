package com.examsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Course {
    private Long courseId;
    private String courseName;
    private String description;
    private LocalDateTime createTime;
}
