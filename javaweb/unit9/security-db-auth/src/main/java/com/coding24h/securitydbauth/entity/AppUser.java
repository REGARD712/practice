package com.coding24h.securitydbauth.entity;

import lombok.Data;
import java.util.List;

@Data
public class AppUser {
    private Long id;
    private String username;
    private String password;
    private Boolean enabled;
    private List<String> roles; // 非数据库字段，用于存储角色列表
}
