package com.coding24h.course.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Course {
    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private BigDecimal price;
    private Long teacherId;
    private User teacher;
    private LocalDateTime createTime;
}
