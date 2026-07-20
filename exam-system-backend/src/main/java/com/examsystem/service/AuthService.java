package com.examsystem.service;

import com.examsystem.dto.LoginUserVO;

public interface AuthService {
    LoginUserVO login(String role, String username, String password);
    void changePassword(String role, Long userId, String oldPassword, String newPassword);
}
