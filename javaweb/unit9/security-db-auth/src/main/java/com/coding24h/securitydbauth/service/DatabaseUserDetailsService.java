package com.coding24h.securitydbauth.service;

import com.coding24h.securitydbauth.entity.AppUser;
import com.coding24h.securitydbauth.mapper.UserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    DatabaseUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = userMapper.findByUsername(username);
        if (appUser == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        List<SimpleGrantedAuthority> authorities = appUser.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return User.builder()
                .username(appUser.getUsername())
                .password(appUser.getPassword())
                .disabled(!appUser.getEnabled())
                .authorities(authorities)
                .build();
    }
}
