package com.coding24h.itnews.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Article {
    private Long id;
    private String title;
    private String content;
    private Date createTime;
    private Long userId;
    private Long categoryId;
    // 关联对象（非数据库字段）
    private User user;
    private Category category;
}
