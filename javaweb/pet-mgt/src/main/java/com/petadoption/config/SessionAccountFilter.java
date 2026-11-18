package com.petadoption.config;

import com.petadoption.entity.User;
import com.petadoption.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 会话账号有效性过滤器。
 * 每次业务请求都按会话中的用户主键读取数据库，确认账号仍然存在、处于启用状态且用户名未被修改。
 * 任一条件不满足时立即销毁会话并跳转登录页，避免改名、停用或删除后用旧身份继续访问受限页面。
 * 本过滤器注册为普通 Servlet Filter，执行顺序排在 Spring Security 过滤器链之后，可读取已建立的认证信息。
 */
@Component
// 普通过滤器默认排在最末，确保在安全上下文恢复之后再校验账号状态。
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class SessionAccountFilter extends OncePerRequestFilter {
    /** 按主键读取账号的当前用户名与启用状态。 */
    private final UserMapper userMapper;

    /**
     * 静态资源与登录注册入口不参与账号校验，避免无意义的数据库查询，
     * 也避免账号失效时阻塞重新登录。
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/images/")
                || uri.startsWith("/uploads/")
                || uri.equals("/style.css")
                || uri.equals("/script.js")
                || uri.equals("/favicon.ico")
                || uri.startsWith("/error")
                || uri.equals("/login")
                || uri.equals("/register");
    }

    /**
     * 校验当前认证账号是否仍然有效；无效时清理安全上下文、销毁会话并跳转登录页。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 仅处理已认证且使用本项目用户对象的请求，匿名访问由安全配置自行处理。
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AuthenticatedUser principal) {
            // 按不可变主键读取账号，避免使用会话中的旧用户名查库。
            User current = userMapper.findById(principal.getId());
            // 账号被删除、停用或改名后，旧会话不再代表有效身份。
            boolean valid = current != null
                    && Boolean.TRUE.equals(current.getEnabled())
                    && principal.getUsername().equals(current.getUsername());
            if (!valid) {
                // 先清理安全上下文，再销毁会话，确保后续请求无法沿用旧身份。
                SecurityContextHolder.clearContext();
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                // 引导用户回到登录页，并携带失效原因。
                response.sendRedirect(request.getContextPath() + "/login?expired");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
