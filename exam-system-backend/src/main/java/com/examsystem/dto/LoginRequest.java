package com.examsystem.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String role;
    private String username;
    private String password;
}
