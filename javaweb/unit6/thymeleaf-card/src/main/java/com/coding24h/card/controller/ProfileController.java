package com.coding24h.card.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
public class ProfileController {

    @GetMapping("/")
    public String index() {
        // redirect: 前缀表示重定向，浏览器会发起新的请求
        return "redirect:/profile";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("name", "李港");
        model.addAttribute("age", 25);
        model.addAttribute("email", "wangxm@example.com");
        model.addAttribute("bio", "我是Java开发工程师，热爱学习和分享技术。");
        List<String> skills = Arrays.asList("Java", "Spring Boot", "Thymeleaf", "MySQL");
        model.addAttribute("skills", skills);
        model.addAttribute("now", LocalDateTime.now());
        return "profile";
    }
}
