package com.coding24h.securitydbauth;

import com.coding24h.securitydbauth.entity.AppUser;
import com.coding24h.securitydbauth.mapper.UserMapper;
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
        if (userMapper.findByUsername("admin") == null) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("1234"));
            admin.setEnabled(true);
            userMapper.insertUser(admin);
            userMapper.insertRole(admin.getId(), "ROLE_ADMIN");
            userMapper.insertRole(admin.getId(), "ROLE_USER");
            System.out.println("初始化管理员用户: admin/1234");
        }
    }
}
