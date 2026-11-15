package com.coding24h.messagesecurity.entity;

import lombok.Data;
import java.util.List;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private Boolean enabled;
    private List<String> roles; // 角色名称列表
}
