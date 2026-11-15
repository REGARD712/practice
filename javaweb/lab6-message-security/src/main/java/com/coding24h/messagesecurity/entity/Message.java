package com.coding24h.messagesecurity.entity;

import java.util.Date;

import lombok.Data;

@Data
public class Message {
    private Long id;
    private String content;
    private Long userId;
    private Date createTime;
    private String username; // 非数据库字段，用于展示发送者名称
}