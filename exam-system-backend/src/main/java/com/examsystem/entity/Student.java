package com.examsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Student {
    private Long studentId;
    private String username;
    private String password;
    private String realName;
    private String gender;
    private String className;
    private String phone;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
}
