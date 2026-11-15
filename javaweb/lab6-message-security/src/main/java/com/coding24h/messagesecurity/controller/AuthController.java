package com.coding24h.messagesecurity.controller;

import com.coding24h.messagesecurity.entity.User;
import com.coding24h.messagesecurity.mapper.UserMapper;
import com.coding24h.messagesecurity.service.MessageService;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageService messageService;

    AuthController(UserMapper userMapper, PasswordEncoder passwordEncoder, MessageService messageService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.messageService = messageService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/messages";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    @Transactional
    public String register(@RequestParam String username, @RequestParam String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        userMapper.insertUser(user);
        userMapper.insertRole(user.getId(), "USER");
        return "redirect:/login?registered";
    }

    @GetMapping("/messages")
    public String messages(Model model, Authentication authentication) {
        model.addAttribute("messages",
                messageService.getMessagesForUser(authentication));
        return "messages";
    }
}
