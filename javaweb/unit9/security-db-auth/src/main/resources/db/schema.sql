-- 创建数据库（请手动在MySQL中执行）
CREATE DATABASE security_db default charset utf8mb4;

USE security_db;
-- 用户表
CREATE TABLE `app_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（加密后）',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用'
);
-- 用户角色表（一个用户可有多个角色）
CREATE TABLE `user_roles` (
    `user_id` BIGINT NOT NULL,
    `role` VARCHAR(50) NOT NULL COMMENT '角色名（如ROLE_ADMIN）',
    FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
);
-- 插入测试数据（账号密码为 admin/1234，实际应使用BCrypt加密，此处仅作结构示例）
-- 注意：真实数据将通过DataInitializer插入加密后的密码，此处不直接插入明文