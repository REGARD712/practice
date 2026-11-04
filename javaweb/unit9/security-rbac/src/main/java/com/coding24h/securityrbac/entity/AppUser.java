package com.coding24h.securityrbac.entity;

import lombok.Data;
import java.util.List;

@Data
public class AppUser {
    private Long id;
    private String username;
    private String password;
    private Boolean enabled;
    private List<String> roles; // 非数据库字段，存储角色名称列表
}
