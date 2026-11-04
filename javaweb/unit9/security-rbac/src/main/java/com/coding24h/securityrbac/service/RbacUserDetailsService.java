package com.coding24h.securityrbac.service;

import com.coding24h.securityrbac.entity.AppUser;
import com.coding24h.securityrbac.mapper.UserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class RbacUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    RbacUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = userMapper.findByUsername(username);
        if (appUser == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        // 根据角色构建权限列表
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String roleName : appUser.getRoles()) {
            // 添加角色本身（Spring Security 会自动加上 ROLE_ 前缀，也可以手动加）
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));

            // 根据角色硬编码授予对应权限
            if ("ADMIN".equals(roleName)) {
                authorities.add(new SimpleGrantedAuthority("document:view"));
                authorities.add(new SimpleGrantedAuthority("document:edit"));
                authorities.add(new SimpleGrantedAuthority("user:manage"));
            } else if ("USER".equals(roleName)) {
                authorities.add(new SimpleGrantedAuthority("document:view"));
            }
        }

        return User.builder()
                .username(appUser.getUsername())
                .password(appUser.getPassword())
                .disabled(!appUser.getEnabled())
                .authorities(authorities)
                .build();
    }
}
