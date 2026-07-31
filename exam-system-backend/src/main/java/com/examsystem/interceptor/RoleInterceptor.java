package com.examsystem.interceptor;

import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 角色权限拦截器。
 * <p>
 * 在登录拦截器通过后执行，根据请求路径前缀校验用户角色：
 * <ul>
 *   <li>{@code /api/admin/**} — 仅允许 admin 角色</li>
 *   <li>{@code /api/teacher/**} — 仅允许 teacher 角色</li>
 *   <li>{@code /api/student/**} — 仅允许 student 角色</li>
 * </ul>
 * <b>admin 是超级管理员：</b>admin 角色可以访问上述所有路径，包括 teacher 和 student 的接口。
 * <p>
 * 如果 session 不存在（极端情况：登录拦截器未生效），返回 401；
 * 如果角色不匹配，返回 403。
 *
 * @see SessionUtil#SESSION_USER_ROLE session 中存储用户角色的 key
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    /** 权限不足时返回的 JSON 响应体 */
    private static final String FORBIDDEN_JSON = "{\"code\":403,\"message\":\"权限不足\",\"data\":null}";
    /** 未登录时返回的 JSON 响应体（兜底：正常情况下 LoginInterceptor 已拦截） */
    private static final String UNAUTH_JSON = "{\"code\":401,\"message\":\"未登录\",\"data\":null}";

    /**
     * 前置拦截：校验 session 中的角色是否匹配请求路径。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应（权限不足时写入 JSON 错误）
     * @param handler  目标处理器
     * @return {@code true} 放行，{@code false} 拦截并返回 401/403
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取已存在的 session（不创建新的）
        HttpSession session = request.getSession(false);
        if (session == null) {
            // 没有 session，说明未登录（兜底逻辑）
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write(UNAUTH_JSON);
            return false;
        }

        String role = (String) session.getAttribute(SessionUtil.SESSION_USER_ROLE);
        String path = request.getRequestURI();

        // admin 是超级用户：可以访问所有角色路径，直接放行
        if ("admin".equals(role)) {
            return true;
        }
        // 非 admin 用户访问 admin 路径 → 403
        if (path.startsWith("/api/admin/") && !"admin".equals(role)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write(FORBIDDEN_JSON);
            return false;
        }
        // 非 teacher 用户访问 teacher 路径 → 403
        if (path.startsWith("/api/teacher/") && !"teacher".equals(role)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write(FORBIDDEN_JSON);
            return false;
        }
        // 非 student 用户访问 student 路径 → 403
        if (path.startsWith("/api/student/") && !"student".equals(role)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write(FORBIDDEN_JSON);
            return false;
        }
        return true; // 放行
    }
}
