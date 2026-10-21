package com.coding24h.icourse.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Course {

    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime createTime;
}
