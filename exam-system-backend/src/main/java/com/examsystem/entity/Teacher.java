package com.examsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Teacher {
    private Long teacherId;
    private String username;
    private String password;
    private String realName;
    private String gender;
    private String phone;
    private String subject;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
}
