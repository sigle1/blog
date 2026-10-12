package com.example.demo.filter;

import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String requestURI = request.getRequestURI();

        // 登录与 QQ 互联接口直接放行
        if (requestURI.equals("/auth/login")
                || requestURI.equals("/auth/qq")
                || requestURI.equals("/auth/qq/callback")
                || requestURI.equals("/auth/qq/config")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 非管理端接口全部放行（游客端无登录校验）
        if (!requestURI.startsWith("/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取Authorization头
        String authorizationHeader = request.getHeader("Authorization");

        // 检查Authorization头
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\": 401, \"message\": \"未授权访问\"}");
            return;
        }

        // 提取令牌
        String token = authorizationHeader.substring(7);

        try {
            // 验证令牌
            if (!jwtUtil.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\": 401, \"message\": \"令牌无效或已过期\"}");
                return;
            }

            // 将用户信息存储到请求中
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            request.setAttribute("username", username);
            request.setAttribute("role", role);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\": 401, \"message\": \"令牌验证失败\"}");
        }
    }
}
