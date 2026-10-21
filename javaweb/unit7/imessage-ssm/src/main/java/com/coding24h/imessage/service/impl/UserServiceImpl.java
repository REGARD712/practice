package com.coding24h.imessage.service.impl;

import com.coding24h.imessage.entity.User;
import com.coding24h.imessage.mapper.UserMapper;
import com.coding24h.imessage.service.UserService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<User> getAll() {
        return userMapper.selectAll();
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}