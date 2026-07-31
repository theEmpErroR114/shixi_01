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

/**
 * 认证控制器 — 处理登录、登出、获取当前用户、修改密码等通用认证操作。
 * <p>
 * 所有角色（admin/teacher/student）共用此控制器。
 * 登录成功后会将用户信息存入 HTTP Session（服务端），前端通过 Cookie 携带 JSESSIONID 来维持会话。
 * <p>
 * <b>注意：</b>只有 {@code /api/auth/login} 不需要登录即可访问，其余接口均需通过 {@link com.examsystem.interceptor.LoginInterceptor} 验证。
 *
 * @see AuthService 认证业务逻辑
 * @see SessionUtil Session key 常量
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录。
     * <p>
     * 验证用户名密码后将用户信息存入 session，前端后续请求会自动携带 Cookie 维持登录状态。
     *
     * @param request 登录请求体（role + username + password）
     * @param session HTTP Session 对象
     * @return 登录成功返回用户信息 VO
     */
    @PostMapping("/login")
    public Result<LoginUserVO> login(@RequestBody LoginRequest request, HttpSession session) {
        // 调用认证服务验证用户名密码，返回用户视图对象
        LoginUserVO user = authService.login(request.getRole(), request.getUsername(), request.getPassword());
        // 将用户关键信息存入 session，供后续请求获取
        session.setAttribute(SessionUtil.SESSION_USER_ID, user.getUserId());
        session.setAttribute(SessionUtil.SESSION_USER_ROLE, user.getRole());
        session.setAttribute(SessionUtil.SESSION_USER_NAME, user.getRealName());
        session.setAttribute(SessionUtil.SESSION_USERNAME, user.getUsername());
        return Result.success(user);
    }

    /**
     * 用户登出。使当前 session 失效，清除所有已存储的用户信息。
     *
     * @param session HTTP Session 对象
     * @return 操作成功
     */
    @PostMapping("/logout")
    public Result<?> logout(HttpSession session) {
        session.invalidate(); // 销毁 session，清除所有 attribute
        return Result.success();
    }

    /**
     * 获取当前登录用户信息。
     * <p>
     * 前端各页面在加载时调用此接口获取用户名、角色等信息以渲染顶部导航栏。
     * 数据直接从 session 中读取，无需查询数据库。
     *
     * @param session HTTP Session 对象
     * @return 当前用户信息 VO，未登录时返回 401
     */
    @GetMapping("/current-user")
    public Result<LoginUserVO> currentUser(HttpSession session) {
        Object userId = session.getAttribute(SessionUtil.SESSION_USER_ID);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        // 从 session 中组装用户信息（性能优于查数据库）
        LoginUserVO user = new LoginUserVO();
        user.setUserId((Long) userId);
        user.setRole((String) session.getAttribute(SessionUtil.SESSION_USER_ROLE));
        user.setRealName((String) session.getAttribute(SessionUtil.SESSION_USER_NAME));
        user.setUsername((String) session.getAttribute(SessionUtil.SESSION_USERNAME));
        return Result.success(user);
    }

    /**
     * 修改当前用户密码。
     * <p>
     * 需要提供旧密码进行验证，新密码长度不能少于 4 位。
     *
     * @param body    请求体（oldPassword + newPassword）
     * @param session HTTP Session 对象
     * @return 操作成功或失败
     */
    @PutMapping("/change-password")
    public Result<?> changePassword(@RequestBody Map<String, String> body, HttpSession session) {
        // 从 session 获取当前用户身份（角色 + 用户ID）
        String role = (String) session.getAttribute(SessionUtil.SESSION_USER_ROLE);
        Long userId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        // 参数校验
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
