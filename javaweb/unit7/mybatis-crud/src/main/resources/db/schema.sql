-- 创建数据库
CREATE DATABASE mybatis_crud_db default charset utf8mb4;
USE mybatis_crud_db;

CREATE TABLE `dept` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    `name` VARCHAR(50) NOT NULL COMMENT '部门名称',
    `location` VARCHAR(100) COMMENT '部门位置',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

INSERT INTO `dept` (`name`, `location`) VALUES
('技术部', '和平路21号10楼'),
('产品部', '工农路89号8楼'),
('市场部', '建设路12号5楼');