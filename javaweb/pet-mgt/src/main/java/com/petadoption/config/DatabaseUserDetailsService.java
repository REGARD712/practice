package com.petadoption.config;

import com.petadoption.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 基于数据库账号的 Spring Security 用户加载服务。
 * 负责把系统中的用户、启用状态和角色转换为认证流程所需的安全用户对象。
 */
@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {
    /** 按用户名查询账号、密码摘要及角色信息。 */
    private final UserMapper userMapper;

    /**
     * 在表单登录认证时按用户名加载账号。
     * 找不到用户与密码错误使用一致提示，避免向登录者泄露账号是否存在。
     *
     * @param username 登录表单提交的用户名
     * @return Spring Security 用于密码校验和授权的用户详情
     * @throws UsernameNotFoundException 用户不存在时抛出，由认证流程统一处理
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询业务用户；Mapper 同时提供该用户关联的角色集合。
        com.petadoption.entity.User user = userMapper.findByUsername(username);
        if (user == null) {
            // 不单独提示“用户不存在”，降低用户名枚举风险。
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        // 将数据库角色名逐项转换为授权对象；ROLE_ADMIN 等前缀供 hasRole 规则识别。
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .map(GrantedAuthority.class::cast)
                .toList();
        // 使用数据库中的 BCrypt 密码摘要，并将启用标记映射为账号可用状态。
        // 后三个布尔参数依次表示账号未过期、凭证未过期、账号未锁定，本项目均固定开启。
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getEnabled()),
                true,
                true,
                true,
                authorities
        );
    }
}
