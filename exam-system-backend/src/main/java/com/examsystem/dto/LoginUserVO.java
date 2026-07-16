package com.examsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserVO {
    private Long userId;
    private String username;
    private String realName;
    private String role;
}
