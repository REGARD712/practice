package com.coding24h.securityrbac.controller;

import com.coding24h.securityrbac.service.UserManageService;
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

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("userList", userManageService.listAllUsers());
        return "admin/dashboard";
    }
}
