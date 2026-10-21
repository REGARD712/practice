CREATE DATABASE icourse_db DEFAULT CHARSET utf8mb4;
USE icourse_db;

-- 用户表
CREATE TABLE `user` (
                        `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                        `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                        `password` VARCHAR(100) NOT NULL COMMENT '密码',
                        `role` ENUM('student', 'teacher', 'admin') DEFAULT 'student' COMMENT '角色',
                        `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
);

-- 课程表
CREATE TABLE `course` (
                          `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课程ID',
                          `name` VARCHAR(100) NOT NULL COMMENT '课程名称',
                          `description` TEXT COMMENT '课程简介',
                          `cover_url` VARCHAR(255) COMMENT '封面图片路径',
                          `teacher_id` BIGINT NOT NULL COMMENT '授课教师ID',
                          `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          FOREIGN KEY (`teacher_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 选课表
CREATE TABLE `enrollment` (
                              `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '选课记录ID',
                              `student_id` BIGINT NOT NULL COMMENT '学生ID',
                              `course_id` BIGINT NOT NULL COMMENT '课程ID',
                              `enroll_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
                              UNIQUE KEY `uk_student_course` (`student_id`, `course_id`),
                              FOREIGN KEY (`student_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                              FOREIGN KEY (`course_id`) REFERENCES `course`(`id`) ON DELETE CASCADE
);

-- 插入测试数据（密码均为123，实际应用需加密）
INSERT INTO `user` (`username`, `password`, `role`) VALUES
                                                        ('张三', '123', 'student'),
                                                        ('李四', '123', 'student'),
                                                        ('王老师', '123', 'teacher'),
                                                        ('管理员', '123', 'admin');

INSERT INTO `course` (`name`, `description`,`cover_url`,`teacher_id`) VALUES
                                                                          ('Java程序设计', 'Java程序设计 - 掌握核心Java开发技术, 本课介绍内容如下...','/uploads/f166c4ffb0c24a90a4eddc8fe4d9d16c.png', 3),
                                                                          ('Spring Boot应用开发基础', 'Spring Boot应用开发基础, 掌握Java微服务开发核心...','/uploads/7b6124bc054a442d99c95d4ef8a5fa6a.png', 3);