package com.examsystem.controller;

import com.examsystem.dto.LoginRequest;
import com.examsystem.dto.LoginUserVO;
import com.examsystem.dto.Result;
import com.examsystem.service.AuthService;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginUserVO> login(@RequestBody LoginRequest request, HttpSession session) {
        LoginUserVO user = authService.login(request.getRole(), request.getUsername(), request.getPassword());
        session.setAttribute(SessionUtil.SESSION_USER_ID, user.getUserId());
        session.setAttribute(SessionUtil.SESSION_USER_ROLE, user.getRole());
        session.setAttribute(SessionUtil.SESSION_USER_NAME, user.getRealName());
        session.setAttribute(SessionUtil.SESSION_USERNAME, user.getUsername());
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpSession session) {
        session.invalidate();
        return Result.success();
    }

    @GetMapping("/current-user")
    public Result<LoginUserVO> currentUser(HttpSession session) {
        Object userId = session.getAttribute(SessionUtil.SESSION_USER_ID);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        LoginUserVO user = new LoginUserVO();
        user.setUserId((Long) userId);
        user.setRole((String) session.getAttribute(SessionUtil.SESSION_USER_ROLE));
        user.setRealName((String) session.getAttribute(SessionUtil.SESSION_USER_NAME));
        user.setUsername((String) session.getAttribute(SessionUtil.SESSION_USERNAME));
        return Result.success(user);
    }

    @PutMapping("/change-password")
    public Result<?> changePassword(@RequestBody Map<String, String> body, HttpSession session) {
        String role = (String) session.getAttribute(SessionUtil.SESSION_USER_ROLE);
        Long userId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null || oldPassword.isEmpty() || newPassword.isEmpty()) {
            return Result.error(400, "密码不能为空");
        }
        if (newPassword.length() < 4) {
            return Result.error(400, "新密码长度至少4位");
        }
        authService.changePassword(role, userId, oldPassword, newPassword);
        return Result.success();
    }
}
