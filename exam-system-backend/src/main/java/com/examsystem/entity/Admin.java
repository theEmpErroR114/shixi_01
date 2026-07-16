package com.examsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Admin {
    private Long adminId;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private LocalDateTime createTime;
}
