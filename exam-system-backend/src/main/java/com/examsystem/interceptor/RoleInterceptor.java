package com.examsystem.interceptor;

import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    private static final String FORBIDDEN_JSON = "{\"code\":403,\"message\":\"权限不足\",\"data\":null}";
    private static final String UNAUTH_JSON = "{\"code\":401,\"message\":\"未登录\",\"data\":null}";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write(UNAUTH_JSON);
            return false;
        }

        String role = (String) session.getAttribute(SessionUtil.SESSION_USER_ROLE);
        String path = request.getRequestURI();

        if (path.startsWith("/api/admin/") && !"admin".equals(role)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write(FORBIDDEN_JSON);
            return false;
        }
        if (path.startsWith("/api/teacher/") && !"teacher".equals(role)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write(FORBIDDEN_JSON);
            return false;
        }
        if (path.startsWith("/api/student/") && !"student".equals(role)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write(FORBIDDEN_JSON);
            return false;
        }
        return true;
    }
}
