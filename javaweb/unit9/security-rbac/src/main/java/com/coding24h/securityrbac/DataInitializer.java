package com.coding24h.securityrbac;

import com.coding24h.securityrbac.entity.AppUser;
import com.coding24h.securityrbac.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    DataInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 初始化管理员用户（如果不存在）
        if (userMapper.findByUsername("admin") == null) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("1234"));
            admin.setEnabled(true);
            userMapper.insertUser(admin);
            userMapper.insertRole(admin.getId(), "ADMIN");
            userMapper.insertRole(admin.getId(), "USER");
            System.out.println("初始化管理员用户: admin/1234 (拥有ADMIN和USER角色)");
        }
        // 初始化普通用户（如果不存在）
        if (userMapper.findByUsername("user") == null) {
            AppUser user = new AppUser();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("1234"));
            user.setEnabled(true);
            userMapper.insertUser(user);
            userMapper.insertRole(user.getId(), "USER");
            System.out.println("初始化普通用户: user/1234 (只有USER角色)");
        }
    }
}
