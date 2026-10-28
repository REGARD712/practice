package com.coding24h.course.controller;

import com.coding24h.course.entity.User;
import com.coding24h.course.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final UserMapper userMapper;

    public AuthController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user == null ? "redirect:/login" : redirectFor(user);
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String authenticate(@RequestParam String username, @RequestParam String password,
                               HttpServletRequest request, Model model) {
        User user = StringUtils.hasText(username) ? userMapper.findByUsername(username) : null;
        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        request.getSession(true).setAttribute("currentUser", user);
        return redirectFor(user);
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private String redirectFor(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "redirect:/admin/users";
            case TEACHER -> "redirect:/teacher/courses";
            case STUDENT -> "redirect:/student/courses";
        };
    }
}
