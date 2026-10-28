package com.coding24h.course.service;

import com.coding24h.course.entity.User;
import com.coding24h.course.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Transactional
    public void create(User user) {
        validate(user, true);
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        userMapper.insert(user);
    }

    @Transactional
    public void update(User user) {
        User existing = userMapper.findById(user.getId());
        if (existing == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        validate(user, false);
        User sameName = userMapper.findByUsername(user.getUsername());
        if (sameName != null && !sameName.getId().equals(user.getId())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(existing.getPassword());
        }
        userMapper.update(user);
    }

    @Transactional
    public void delete(Long id, Long actingAdminId) {
        if (actingAdminId.equals(id)) {
            throw new IllegalArgumentException("不能删除当前登录的管理员账号");
        }
        if (userMapper.findById(id) == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        userMapper.deleteById(id);
    }

    private void validate(User user, boolean passwordRequired) {
        if (!StringUtils.hasText(user.getUsername()) || user.getUsername().length() > 50) {
            throw new IllegalArgumentException("用户名不能为空且不能超过50个字符");
        }
        if (passwordRequired && !StringUtils.hasText(user.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (StringUtils.hasText(user.getPassword()) && user.getPassword().length() > 100) {
            throw new IllegalArgumentException("密码不能超过100个字符");
        }
        if (user.getRole() == null) {
            throw new IllegalArgumentException("请选择用户角色");
        }
    }
}
