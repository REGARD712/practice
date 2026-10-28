package com.coding24h.course.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private Role role;
    private LocalDateTime createTime;

    public enum Role {
        STUDENT("student"),
        TEACHER("teacher"),
        ADMIN("admin");

        private final String value;

        Role(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Role fromValue(String value) {
            for (Role role : values()) {
                if (role.value.equalsIgnoreCase(value)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("未知用户角色: " + value);
        }
    }
}
