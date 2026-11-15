package com.coding24h.messagesecurity;

import com.coding24h.messagesecurity.entity.Message;
import com.coding24h.messagesecurity.entity.User;
import com.coding24h.messagesecurity.mapper.MessageMapper;
import com.coding24h.messagesecurity.mapper.UserMapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;
    private final PasswordEncoder passwordEncoder;

    DataInitializer(UserMapper userMapper, MessageMapper messageMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.messageMapper = messageMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 初始化管理员
        if (userMapper.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("1234"));
            admin.setEnabled(true);
            userMapper.insertUser(admin);
            userMapper.insertRole(admin.getId(), "ADMIN");
            userMapper.insertRole(admin.getId(), "USER");
        }
        // 初始化普通用户
        if (userMapper.findByUsername("alice") == null) {
            User user = new User();
            user.setUsername("alice");
            user.setPassword(passwordEncoder.encode("1234"));
            user.setEnabled(true);
            userMapper.insertUser(user);
            userMapper.insertRole(user.getId(), "USER");
        }
        // 初始化演示消息
        if (messageMapper.findAllMessages().isEmpty()) {
            Message msg = new Message();
            msg.setContent("欢迎使用消息系统！");
            msg.setUserId(userMapper.findByUsername("admin").getId());
            messageMapper.insertMessage(msg);
        }
    }
}
