package com.coding24h.securityrbac.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class UserManageService {

    @PreAuthorize("hasAuthority('user:manage')")
    public String listAllUsers() {
        return "这是所有用户的列表（仅管理员可见）。";
    }
}