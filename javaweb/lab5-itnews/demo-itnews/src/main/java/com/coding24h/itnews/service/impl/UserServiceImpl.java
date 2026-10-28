package com.coding24h.itnews.service.impl;

import com.coding24h.itnews.entity.User;
import com.coding24h.itnews.mapper.UserMapper;
import com.coding24h.itnews.service.UserService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper usermapper) {
        this.userMapper = usermapper;
    }

    @Override
    public List<User> findAll() {
        return userMapper.selectAll();
    }

    @Override
    public User findById(Long id) {
        return userMapper.selectById(id);
    }
}