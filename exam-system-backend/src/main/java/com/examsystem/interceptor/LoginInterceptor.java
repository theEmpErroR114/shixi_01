package com.examsystem.interceptor;

import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器。
 * <p>
 * 在请求到达 Controller 之前执行，检查当前 session 是否存在且包含已登录用户ID。
 * 如果未登录，直接返回 JSON 格式的 401 错误响应，不继续执行后续拦截器和 Controller。
 * <p>
 * <b>拦截范围：</b>{@code /api/**}（在 {@link com.examsystem.config.WebMvcConfig} 中注册）
 * <b>排除路径：</b>{@code /api/auth/login}
 *
 * @see SessionUtil#SESSION_USER_ID session 中存储用户ID的 key
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /** 未登录时返回的 JSON 响应体，预编译为常量避免重复拼接字符串 */
    private static final String UNAUTH_JSON = "{\"code\":401,\"message\":\"未登录，请先登录\",\"data\":null}";

    /**
     * 前置拦截：检查 session 中是否存在用户ID。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应（未登录时直接写入 401 JSON）
     * @param handler  目标处理器
     * @return {@code true} 放行继续执行，{@code false} 拦截并返回 401
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // getSession(false) 表示不创建新 session，仅获取已存在的
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionUtil.SESSION_USER_ID) == null) {
            // 未登录：设置响应类型为 JSON，返回 401 状态码和错误信息
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write(UNAUTH_JSON);
            return false; // 拦截请求
        }
        return true; // 放行
    }
}
