CREATE DATABASE IF NOT EXISTS `imessage_ssm_db` DEFAULT CHARSET utf8mb4;
USE `imessage_ssm_db`;

-- 用户表（id为INT UNSIGNED）
CREATE TABLE `user` (
                        `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
                        `username` VARCHAR(50) NOT NULL UNIQUE,
                        `password` VARCHAR(100) NOT NULL,
                        `email` VARCHAR(100),
                        `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 分类表
CREATE TABLE `category` (
                            `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
                            `name` VARCHAR(50) NOT NULL UNIQUE
);

-- 消息表（增加category_id外键）
CREATE TABLE `message` (
                           `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
                           `title` VARCHAR(100) NOT NULL,
                           `content` TEXT NOT NULL,
                           `user_id` INT UNSIGNED NOT NULL,
                           `category_id` INT UNSIGNED NOT NULL,
                           `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                           FOREIGN KEY (`category_id`) REFERENCES `category`(`id`) ON DELETE RESTRICT
);

-- 测试数据：3个用户、3个分类、5条消息
INSERT INTO `user` (`username`, `password`, `email`) VALUES
                                                         ('张三', '123456', 'zhang@example.com'),
                                                         ('李四', '123456', 'li@example.com'),
                                                         ('王五', '123456', 'wang@example.com');

INSERT INTO `category` (`name`) VALUES
                                    ('技术分享'),
                                    ('生活随笔'),
                                    ('工作记录');

INSERT INTO `message` (`title`, `content`, `user_id`, `category_id`) VALUES
                                                                         ('MyBatis注解实战', '使用@Select、@Insert等注解简化开发，非常方便。', 1, 1),
                                                                         ('周末爬山记', '周末和同事一起爬了西山，风景很好，身心放松。', 2, 2),
                                                                         ('Spring Boot 配置总结', '记录Spring Boot中常用配置项，方便查阅。', 1, 1),
                                                                         ('项目周报模板', '整理了一份周报模板，供团队参考。', 3, 3),
                                                                         ('学习计划', '接下来一个月重点学习微服务架构。', 2, 1);