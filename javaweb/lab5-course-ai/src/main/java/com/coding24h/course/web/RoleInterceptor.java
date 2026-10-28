package com.coding24h.course.web;

import com.coding24h.course.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class RoleInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        String path = request.getRequestURI();
        boolean allowed = path.startsWith("/admin/") ? user.getRole() == User.Role.ADMIN
                : path.startsWith("/teacher/") ? user.getRole() == User.Role.TEACHER
                : path.startsWith("/student/") ? user.getRole() == User.Role.STUDENT
                : true;
        if (!allowed) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "当前用户角色无权访问此页面");
        }
        return allowed;
    }
}
