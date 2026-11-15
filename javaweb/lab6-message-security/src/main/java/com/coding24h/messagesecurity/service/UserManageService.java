package com.coding24h.messagesecurity.service;

import java.util.List;

import com.coding24h.messagesecurity.entity.User;
import com.coding24h.messagesecurity.mapper.UserMapper;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class UserManageService {
    private final UserMapper userMapper;

    UserManageService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @PreAuthorize("hasAuthority('user:manage')")
    public List<User> getAllUsers() {
        return userMapper.findAllUsers();
    }
}
