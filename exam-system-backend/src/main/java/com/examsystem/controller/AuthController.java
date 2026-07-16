package com.examsystem.controller;

import com.examsystem.dto.LoginRequest;
import com.examsystem.dto.LoginUserVO;
import com.examsystem.dto.Result;
import com.examsystem.service.AuthService;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
