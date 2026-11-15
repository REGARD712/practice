package com.petadoption.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Web 安全与访问权限配置。
 * 统一配置密码加密、数据库认证、URL 授权、表单登录、退出登录和拒绝访问页面。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * 提供全局密码编码器，注册或修改密码时加密，登录时由安全框架执行匹配。
     *
     * @return 带随机盐并支持强度自适应的 BCrypt 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 只保存 BCrypt 摘要，不在业务代码中保存或比较明文密码。
        return new BCryptPasswordEncoder();
    }

    /**
     * 构建应用的 HTTP 安全过滤器链。
     * 授权规则按声明顺序匹配，因此先限制管理端和申请功能，再开放公共资源，最后兜底认证。
     *
     * @param http Spring Security 提供的 HTTP 安全配置构建器
     * @param userDetailsService 从数据库加载账号及角色的认证服务
     * @return 已配置完成的安全过滤器链
     * @throws Exception 安全配置构建失败时向上抛出
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   DatabaseUserDetailsService userDetailsService) throws Exception {
        http
                // 指定数据库用户源，使表单登录能够校验系统账号及启用状态。
                .userDetailsService(userDetailsService)
                // 配置各组 URL 的访问权限；同一请求采用最先匹配的规则。
                .authorizeHttpRequests(authorize -> authorize
                        // /admin 下所有页面和操作仅允许具有 ROLE_ADMIN 的管理员访问。
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 查看本人申请、展示申请页及提交申请均要求用户已经登录，不区分具体角色。
                        .requestMatchers("/applications/**", "/pets/*/apply").authenticated()
                        // 首页、认证页面、公开宠物浏览、静态资源、上传图片和错误页允许匿名访问。
                        .requestMatchers("/", "/login", "/register", "/pets", "/pets/{id}",
                                "/style.css", "/script.js", "/images/**", "/uploads/**", "/error")
                        .permitAll()
                        // 未显式列出的其他请求默认需要登录，避免新增路由被意外公开。
                        .anyRequest().authenticated()
                )
                // 启用基于表单的用户名密码认证。
                .formLogin(form -> form
                        // 使用项目自定义登录页，而不是安全框架默认页面。
                        .loginPage("/login")
                        // 登录成功后始终进入首页，true 表示不恢复原请求地址。
                        .defaultSuccessUrl("/", true)
                        // 登录页及登录处理地址允许匿名访问。
                        .permitAll()
                )
                // 配置退出登录，使会话失效并清理当前安全上下文。
                .logout(logout -> logout
                        // 接收退出请求的地址，默认要求 POST，可结合 CSRF 防护避免被动退出。
                        .logoutUrl("/logout")
                        // 退出成功后返回登录页，并通过参数提示已退出。
                        .logoutSuccessUrl("/login?logout")
                        // 退出接口对当前访问者开放，由框架处理实际注销逻辑。
                        .permitAll()
                )
                // 已认证但权限不足时展示自定义 403 页面；未认证访问仍由登录入口处理。
                .exceptionHandling(exceptions -> exceptions.accessDeniedPage("/403"));
        // 固化上述配置并生成过滤器链 Bean。
        return http.build();
    }
}
