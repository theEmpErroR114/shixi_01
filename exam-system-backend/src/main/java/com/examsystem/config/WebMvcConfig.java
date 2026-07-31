package com.examsystem.config;

import com.examsystem.interceptor.LoginInterceptor;
import com.examsystem.interceptor.RoleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 全局配置类。
 * <p>
 * 负责两项核心配置：
 * <ol>
 *   <li><b>CORS 跨域配置</b>：允许所有来源访问 {@code /api/**} 路径，支持携带 Cookie（credentials）。
 *       当前配置为开发阶段全放通，生产环境应限制 allowedOriginPatterns。</li>
 *   <li><b>拦截器注册</b>：按顺序注册登录拦截器和角色拦截器，保护所有 {@code /api/**} 接口。
 *       只有 {@code /api/auth/login} 被排除，允许未登录访问。</li>
 * </ol>
 *
 * @see LoginInterceptor 登录拦截器（检查 session 中是否有用户ID）
 * @see RoleInterceptor  角色拦截器（检查用户角色是否有权访问对应路径）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 登录拦截器：验证请求是否已登录（session 中是否有用户ID） */
    @Autowired
    private LoginInterceptor loginInterceptor;

    /** 角色拦截器：验证用户角色是否匹配请求路径（admin/teacher/student） */
    @Autowired
    private RoleInterceptor roleInterceptor;

    /**
     * 配置跨域资源共享（CORS）规则。
     * <p>
     * 当前为开发环境配置，允许所有来源、所有方法、所有请求头，
     * 并开启 credentials 支持（前端 fetch 需设置 credentials: 'include'）。
     * 生产环境部署时应将 allowedOriginPatterns 缩小为具体域名。
     *
     * @param registry CORS 注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")                                    // 对所有 /api/ 路径生效
                .allowedOriginPatterns("*")                               // 允许所有来源（开发环境）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的 HTTP 方法
                .allowedHeaders("*")                                      // 允许所有请求头
                .allowCredentials(true);                                  // 允许携带 Cookie/Session
    }

    /**
     * 注册拦截器链。
     * <p>
     * 执行顺序为注册顺序：
     * <ol>
     *   <li>{@link LoginInterceptor}：检查 session 是否存在及是否包含用户ID</li>
     *   <li>{@link RoleInterceptor}：检查用户角色是否有权访问目标路径</li>
     * </ol>
     * 登录接口 {@code /api/auth/login} 被两者排除，允许匿名访问。
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器：拦截所有 /api/**，但排除登录接口
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");

        // 角色拦截器：只拦截角色相关路径前缀，排除登录接口
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/admin/**", "/api/teacher/**", "/api/student/**")
                .excludePathPatterns("/api/auth/login");
    }
}
