CREATE DATABASE IF NOT EXISTS course_db DEFAULT CHARSET utf8mb4;

USE course_db;
-- 用户表
CREATE TABLE `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('student', 'teacher', 'admin') DEFAULT 'student',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
-- 课程表
CREATE TABLE course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    cover_url VARCHAR(255),
    price DECIMAL(10, 2) DEFAULT 0.00,
    teacher_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES user (id) ON DELETE CASCADE
);
-- 选课表
CREATE TABLE enrollment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enroll_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_course (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES user (id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE
);
-- 插入测试数据（密码均为123）
INSERT INTO
    user (username, password, role)
VALUES ('admin', '123', 'admin'),
    ('张老师', '123', 'teacher'),
    ('李老师', '123', 'teacher'),
    ('王小明', '123', 'student');

INSERT INTO
    course (
        name,
        description,
        cover_url,
        price,
        teacher_id
    )
VALUES (
        'Java程序设计',
        '从零开始学习Java，掌握核心开发技术',
        '/images/course-placeholder.svg',
        199.00,
        2
    ),
    (
        'Spring Boot实战',
        '快速构建微服务应用',
        '/images/course-placeholder.svg',
        299.00,
        3
    );