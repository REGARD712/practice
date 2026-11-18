package com.petadoption.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 认证成功后保存在会话中的用户身份。
 * 除用户名外额外保存不可变的主键：账号改名后主键不变，
 * 业务代码据此定位当前用户，避免继续使用会话里的旧用户名查询数据库。
 */
public class AuthenticatedUser implements UserDetails, Serializable {
    /** 用户详情需要序列化到会话，固定版本号保证兼容。 */
    private static final long serialVersionUID = 1L;

    /** 用户主键，登录后不会随用户名修改而变化。 */
    private final Long id;
    /** 登录用户名，用于会话失效校验时与数据库比对。 */
    private final String username;
    /** BCrypt 密码摘要，仅交给 Spring Security 校验。 */
    private final String password;
    /** 账号是否启用。 */
    private final boolean enabled;
    /** 角色授权列表，供 hasRole 等规则使用。 */
    private final List<GrantedAuthority> authorities;

    /**
     * 使用数据库账号信息构造安全用户对象。
     *
     * @param id 用户主键
     * @param username 用户名
     * @param password 密码摘要
     * @param enabled 账号是否启用
     * @param authorities 角色授权列表
     */
    public AuthenticatedUser(Long id, String username, String password, boolean enabled,
                             List<GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    /** 返回不可变的用户主键，业务层以此作为会话身份。 */
    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
