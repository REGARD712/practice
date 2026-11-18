CREATE DATABASE IF NOT EXISTS pet_adoption
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pet_adoption;
SET NAMES utf8mb4;

DROP TABLE IF EXISTS adoption_application;
DROP TABLE IF EXISTS pet_image;
DROP TABLE IF EXISTS pet;
DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS breed;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `role`;

-- 创建角色表，用于保存普通用户、管理员等角色定义。
CREATE TABLE `role` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE
) ENGINE = InnoDB;

-- 创建用户表，用于保存平台账号、登录凭据、联系方式及账号状态。
CREATE TABLE `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(30),
    avatar VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_enabled (enabled)
) ENGINE = InnoDB;

-- 创建用户角色关联表，实现用户与角色之间的多对多关系。
CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES `role` (id) ON DELETE CASCADE,
    INDEX idx_user_role_role (role_id)
) ENGINE = InnoDB;

-- 创建宠物品种表，统一维护猫、犬等物种下的品种资料。
CREATE TABLE breed (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    species VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    UNIQUE KEY uk_breed_name_species (name, species)
) ENGINE = InnoDB;

-- 创建宠物信息表，保存待领养宠物的基本资料、健康情况、介绍及领养状态。
CREATE TABLE pet (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    breed_id BIGINT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    age_months INT NOT NULL,
    health_status VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pet_breed FOREIGN KEY (breed_id) REFERENCES breed (id) ON DELETE RESTRICT,
    CONSTRAINT chk_pet_status CHECK (status IN ('AVAILABLE', 'PENDING', 'ADOPTED')),
    INDEX idx_pet_status_created (status, created_at),
    INDEX idx_pet_breed (breed_id),
    INDEX idx_pet_name (name)
) ENGINE = InnoDB;

-- 创建宠物图片表，用于保存每只宠物的一张或多张图片及其封面标记。
CREATE TABLE pet_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pet_id BIGINT NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    cover BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_pet_image_pet FOREIGN KEY (pet_id) REFERENCES pet (id) ON DELETE CASCADE,
    INDEX idx_pet_image_pet_cover (pet_id, cover)
) ENGINE = InnoDB;

-- 创建领养申请表，记录用户针对宠物提交的申请资料及管理员审核结果。
CREATE TABLE adoption_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pet_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    contact_phone VARCHAR(30) NOT NULL,
    address VARCHAR(255) NOT NULL,
    housing_condition VARCHAR(1000) NOT NULL,
    experience VARCHAR(1000),
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    review_comment VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    CONSTRAINT fk_application_pet FOREIGN KEY (pet_id) REFERENCES pet (id) ON DELETE RESTRICT,
    CONSTRAINT fk_application_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE RESTRICT,
    CONSTRAINT chk_application_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    INDEX idx_application_pet_status (pet_id, status),
    INDEX idx_application_user_created (user_id, created_at),
    INDEX idx_application_status_created (status, created_at)
) ENGINE = InnoDB;

-- 向角色表写入两个固定角色，显式指定 id 和 role_name，便于后续稳定建立用户角色关联。
INSERT INTO `role` (id, role_name) VALUES
    (1, 'ROLE_USER'),
    (2, 'ROLE_ADMIN');

-- 测试账号密码：管理员为 admin/admin123，普通用户为 user/user123；password 字段保存已生成并验证的 BCrypt 哈希。
-- 插入用户种子数据，显式指定用户编号、用户名、BCrypt 密码哈希、邮箱、电话、头像及启用状态。
INSERT INTO `user` (id, username, password, email, phone, avatar, enabled) VALUES
    (1, 'admin', '$2b$10$IdtOiZDGwwvsaEDR8OSyWu331Vyb7xhGc7lRWNsQPaDIRem0g8iY2', 'admin@petadoption.local', '13800000001', NULL, TRUE),
    (2, 'user', '$2b$10$WW3WoPemkYK2uckS0pMdkuSs3OKEQESz3mOTDf1.ZsakCZ4uIrh/a', 'user@petadoption.local', '13800000002', NULL, TRUE);

-- 写入用户角色关联种子数据，为上述测试账号分配对应角色。
INSERT INTO user_role (user_id, role_id) VALUES
    (1, 2),
    (2, 1);

-- 写入品种种子数据，显式指定品种编号、名称、物种及品种介绍。
INSERT INTO breed (id, name, species, description) VALUES
    (1, '英国短毛猫', '猫', '性格温和安静，适应能力强，被毛浓密。'),
    (2, '暹罗猫', '猫', '聪明亲人，活泼好奇，喜欢与主人互动。'),
    (3, '柯基犬', '犬', '开朗友善，精力充沛，适合有规律陪伴的家庭。'),
    (4, '金毛寻回犬', '犬', '温顺聪明，耐心友好，需要充足运动。'),
    (5, '柴犬', '犬', '独立机警，忠诚爱干净，需要稳定的社会化训练。'),
    (6, '西伯利亚哈士奇', '犬', '活泼外向，运动量较大，适合时间充裕的领养人。'),
    (7, '贵宾犬', '犬', '聪慧亲人，学习能力强，需要定期梳理被毛。');

-- 写入宠物种子数据，依次设置宠物编号、名称、品种编号、性别、月龄、健康状况、介绍及领养状态。
INSERT INTO pet (id, name, breed_id, gender, age_months, health_status, description, status) VALUES
    (1, '糯米', 1, '雌性', 18, '已接种疫苗，已绝育', '性格安静亲人，喜欢趴在窗边晒太阳，能够适应室内家庭生活。', 'AVAILABLE'),
    (2, '豆豆', 2, '雄性', 14, '已接种疫苗，已驱虫', '活泼好奇，喜欢互动玩具，希望领养人每天能安排陪伴时间。', 'AVAILABLE'),
    (3, '布丁', 3, '雄性', 20, '已接种疫苗，体检健康', '友善外向，喜欢散步，已经掌握基本随行和定点如厕。', 'AVAILABLE'),
    (4, '阳光', 4, '雌性', 26, '已接种疫苗，已绝育', '温柔耐心，对孩子和其他宠物友好，需要每天适量运动。', 'AVAILABLE'),
    (5, '小枫', 5, '雄性', 16, '已接种疫苗，已驱虫', '聪明独立，熟悉基础口令，适合愿意持续训练的家庭。', 'AVAILABLE'),
    (6, '雪花', 6, '雌性', 22, '已接种疫苗，体检健康', '精力旺盛，喜欢户外活动，需要安全的活动空间和充足陪伴。', 'AVAILABLE'),
    (7, '可可', 7, '雄性', 12, '已接种疫苗，已驱虫', '亲人聪明，学习指令很快，需要定期美容和温和引导。', 'AVAILABLE');

-- 写入宠物图片种子数据，为每只宠物指定图片访问路径，并将这些图片设为封面。
INSERT INTO pet_image (pet_id, image_path, cover) VALUES
    (1, '/images/products/british-shorthair.jpg', TRUE),
    (2, '/images/products/siamese.jpg', TRUE),
    (3, '/images/products/corgi.jpg', TRUE),
    (4, '/images/products/golden-retriever.jpg', TRUE),
    (5, '/images/products/shiba-inu.jpg', TRUE),
    (6, '/images/products/husky.jpg', TRUE),
    (7, '/images/products/poodle.jpg', TRUE);
