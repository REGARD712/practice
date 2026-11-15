package com.coding24h.messagesecurity.controller;

import com.coding24h.messagesecurity.service.UserManageService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserManageService userManageService;

    AdminController(UserManageService userManageService) {
        this.userManageService = userManageService;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userManageService.getAllUsers());
        return "admin/users";
    }
}
