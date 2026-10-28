package com.coding24h.course.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Enrollment {
    private Long id;
    private Long studentId;
    private Long courseId;
    private User student;
    private Course course;
    private LocalDateTime enrollTime;
}
