package com.coding24h.imessage.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Message {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private Long categoryId;
    private Date createTime;
    private User user;       // 关联用户
    private Category category; // 关联分类
}
