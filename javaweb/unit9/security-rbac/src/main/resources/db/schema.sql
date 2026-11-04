-- 创建数据库（请手动在MySQL中执行）
CREATE DATABASE security_rbac_db default charset utf8mb4;

USE security_rbac_db;

-- 1. 用户表
CREATE TABLE `app_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用'
);

-- 2. 角色表
CREATE TABLE `role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名（如 ADMIN, USER）'
);

-- 3. 用户-角色关联表（多对多）
CREATE TABLE `user_role` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`),
    FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
);

-- 插入基础角色数据
INSERT INTO `role` (`name`) VALUES ('ADMIN'), ('USER');