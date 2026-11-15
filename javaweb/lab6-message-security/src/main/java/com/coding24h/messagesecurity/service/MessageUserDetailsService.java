package com.coding24h.messagesecurity.service;

import java.util.ArrayList;
import java.util.List;

import com.coding24h.messagesecurity.entity.User;
import com.coding24h.messagesecurity.mapper.UserMapper;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MessageUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    MessageUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String roleName : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
            if ("ADMIN".equals(roleName)) {
                authorities.add(new SimpleGrantedAuthority("message:delete"));
                authorities.add(new SimpleGrantedAuthority("user:manage"));
            } else if ("USER".equals(roleName)) {
                authorities.add(new SimpleGrantedAuthority("message:delete_own"));
            }
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.getEnabled())
                .authorities(authorities)
                .build();
    }
}