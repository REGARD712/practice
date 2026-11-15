-- 创建宠物领养平台数据库 pet_adoption；如果同名数据库已经存在，则跳过创建，避免重复执行时报错。
CREATE DATABASE IF NOT EXISTS pet_adoption
    -- 将数据库默认字符集设为 utf8mb4，以完整支持中文、emoji 及其他 Unicode 字符。
    DEFAULT CHARACTER SET utf8mb4
    -- 将数据库默认排序规则设为 utf8mb4_unicode_ci，用于不区分大小写的 Unicode 字符比较与排序。
    COLLATE utf8mb4_unicode_ci;

-- 切换到 pet_adoption 数据库，确保后续建表和种子数据都写入该数据库。
USE pet_adoption;
-- 将当前连接的客户端、连接及结果字符集统一设为 utf8mb4，避免中文数据读写乱码。
SET NAMES utf8mb4;

-- 按外键依赖从子表到父表的顺序清理旧表；首先删除领养申请表，避免引用宠物或用户时阻碍删表。
DROP TABLE IF EXISTS adoption_application;
-- 删除宠物图片表；IF EXISTS 保证表不存在时脚本仍可继续执行。
DROP TABLE IF EXISTS pet_image;
-- 删除宠物表；其图片表和领养申请表已先行删除。
DROP TABLE IF EXISTS pet;
-- 删除用户角色关联表，解除用户表与角色表之间的外键依赖。
DROP TABLE IF EXISTS user_role;
-- 删除品种表；引用它的宠物表已先行删除。
DROP TABLE IF EXISTS breed;
-- 删除用户表；反引号用于避免 user 与数据库关键字或内置名称产生歧义。
DROP TABLE IF EXISTS `user`;
-- 最后删除角色表；引用它的用户角色关联表已先行删除。
DROP TABLE IF EXISTS `role`;

-- 创建角色表，用于保存普通用户、管理员等角色定义。
CREATE TABLE `role` (
    -- 角色主键：使用 BIGINT 存储，并由数据库自动递增生成唯一编号。
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 角色名称：最长 50 个字符，不允许为空，并通过 UNIQUE 约束保证名称唯一。
    role_name VARCHAR(50) NOT NULL UNIQUE
    -- 使用 InnoDB 存储引擎，以支持事务、行级锁和外键约束。
) ENGINE = InnoDB;

-- 创建用户表，用于保存平台账号、登录凭据、联系方式及账号状态。
CREATE TABLE `user` (
    -- 用户主键：使用 BIGINT 存储，并由数据库自动递增生成唯一编号。
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 登录用户名：最长 50 个字符，不允许为空，且在全表范围内唯一。
    username VARCHAR(50) NOT NULL UNIQUE,
    -- 登录密码摘要：最长 100 个字符且不允许为空，保存 BCrypt 哈希而非明文密码。
    password VARCHAR(100) NOT NULL,
    -- 电子邮箱：最长 100 个字符，不允许为空，并通过 UNIQUE 约束防止重复注册。
    email VARCHAR(100) NOT NULL UNIQUE,
    -- 联系电话：最长 30 个字符，允许为空，便于后续补充联系方式。
    phone VARCHAR(30),
    -- 头像访问路径：最长 255 个字符，允许为空，未设置头像时保存 NULL。
    avatar VARCHAR(255),
    -- 账号启用标记：不允许为空，默认 TRUE 表示启用，FALSE 表示禁用。
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    -- 账号创建时间：不允许为空，插入记录时默认使用数据库当前时间。
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 为 enabled 字段创建普通索引 idx_user_enabled，加速按账号启用状态筛选用户。
    INDEX idx_user_enabled (enabled)
    -- 使用 InnoDB 存储引擎，为用户及其关联数据提供事务和外键支持。
) ENGINE = InnoDB;

-- 创建用户角色关联表，实现用户与角色之间的多对多关系。
CREATE TABLE user_role (
    -- 用户编号：不允许为空，与 user 表主键对应，标识关联记录所属用户。
    user_id BIGINT NOT NULL,
    -- 角色编号：不允许为空，与 role 表主键对应，标识授予用户的角色。
    role_id BIGINT NOT NULL,
    -- 以用户编号和角色编号组成联合主键，同时防止同一用户重复绑定同一角色。
    PRIMARY KEY (user_id, role_id),
    -- 用户外键 fk_user_role_user：引用 user.id；删除用户时级联删除其全部角色关联记录。
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
    -- 角色外键 fk_user_role_role：引用 role.id；删除角色时级联删除该角色的全部用户关联记录。
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES `role` (id) ON DELETE CASCADE,
    -- 为 role_id 创建普通索引 idx_user_role_role，加速查询拥有指定角色的用户及外键检查。
    INDEX idx_user_role_role (role_id)
    -- 使用 InnoDB 存储引擎，确保联合主键和外键级联行为得到支持。
) ENGINE = InnoDB;

-- 创建宠物品种表，统一维护猫、犬等物种下的品种资料。
CREATE TABLE breed (
    -- 品种主键：使用 BIGINT 存储，并由数据库自动递增生成唯一编号。
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 品种名称：最长 80 个字符，不允许为空，例如“英国短毛猫”或“柯基犬”。
    name VARCHAR(80) NOT NULL,
    -- 所属物种：最长 50 个字符，不允许为空，例如“猫”或“犬”。
    species VARCHAR(50) NOT NULL,
    -- 品种介绍：最长 500 个字符，允许为空，用于描述性格、护理及运动需求等特点。
    description VARCHAR(500),
    -- 唯一键 uk_breed_name_species：对名称和物种建立联合唯一约束，避免同一物种下出现重复品种。
    UNIQUE KEY uk_breed_name_species (name, species)
    -- 使用 InnoDB 存储引擎，为品种与宠物之间的引用完整性提供支持。
) ENGINE = InnoDB;

-- 创建宠物信息表，保存待领养宠物的基本资料、健康情况、介绍及领养状态。
CREATE TABLE pet (
    -- 宠物主键：使用 BIGINT 存储，并由数据库自动递增生成唯一编号。
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 宠物名称：最长 80 个字符，不允许为空。
    name VARCHAR(80) NOT NULL,
    -- 品种编号：不允许为空，通过外键关联 breed 表，标明宠物所属品种。
    breed_id BIGINT NOT NULL,
    -- 性别：最长 20 个字符，不允许为空，种子数据使用“雌性”或“雄性”。
    gender VARCHAR(20) NOT NULL,
    -- 月龄：使用整数记录宠物年龄，单位为月，不允许为空。
    age_months INT NOT NULL,
    -- 健康状况：最长 100 个字符，不允许为空，用于记录疫苗、驱虫、绝育及体检情况。
    health_status VARCHAR(100) NOT NULL,
    -- 宠物详细介绍：使用 TEXT 类型且不允许为空，用于描述性格、习惯及领养陪伴需求。
    description TEXT NOT NULL,
    -- 领养状态：最长 20 个字符且不允许为空，默认 AVAILABLE 表示当前可领养。
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    -- 档案创建时间：不允许为空，插入记录时默认使用数据库当前时间。
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 品种外键 fk_pet_breed：引用 breed.id；存在宠物引用时 RESTRICT 阻止删除对应品种。
    CONSTRAINT fk_pet_breed FOREIGN KEY (breed_id) REFERENCES breed (id) ON DELETE RESTRICT,
    -- 状态检查约束 chk_pet_status：仅允许 AVAILABLE（可领养）、PENDING（审核中）、ADOPTED（已领养）。
    CONSTRAINT chk_pet_status CHECK (status IN ('AVAILABLE', 'PENDING', 'ADOPTED')),
    -- 联合索引 idx_pet_status_created：按领养状态和创建时间组合检索，加速宠物列表筛选与排序。
    INDEX idx_pet_status_created (status, created_at),
    -- 普通索引 idx_pet_breed：加速按品种查询宠物，并支持品种外键检查。
    INDEX idx_pet_breed (breed_id),
    -- 普通索引 idx_pet_name：加速按宠物名称进行查询。
    INDEX idx_pet_name (name)
    -- 使用 InnoDB 存储引擎，支持宠物数据事务、外键约束及索引。
) ENGINE = InnoDB;

-- 创建宠物图片表，用于保存每只宠物的一张或多张图片及其封面标记。
CREATE TABLE pet_image (
    -- 图片主键：使用 BIGINT 存储，并由数据库自动递增生成唯一编号。
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 宠物编号：不允许为空，通过外键关联 pet 表，标识图片所属宠物。
    pet_id BIGINT NOT NULL,
    -- 图片访问路径：最长 255 个字符且不允许为空，保存图片在应用中的相对 URL 路径。
    image_path VARCHAR(255) NOT NULL,
    -- 封面标记：不允许为空，默认 FALSE 表示普通图片，TRUE 表示作为宠物封面展示。
    cover BOOLEAN NOT NULL DEFAULT FALSE,
    -- 宠物外键 fk_pet_image_pet：引用 pet.id；删除宠物时级联删除其全部图片记录。
    CONSTRAINT fk_pet_image_pet FOREIGN KEY (pet_id) REFERENCES pet (id) ON DELETE CASCADE,
    -- 联合索引 idx_pet_image_pet_cover：加速按宠物编号和封面标记查找图片。
    INDEX idx_pet_image_pet_cover (pet_id, cover)
    -- 使用 InnoDB 存储引擎，确保宠物与图片之间的外键级联删除生效。
) ENGINE = InnoDB;

-- 创建领养申请表，记录用户针对宠物提交的申请资料及管理员审核结果。
CREATE TABLE adoption_application (
    -- 申请主键：使用 BIGINT 存储，并由数据库自动递增生成唯一编号。
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 宠物编号：不允许为空，通过外键关联 pet 表，标识本次申请希望领养的宠物。
    pet_id BIGINT NOT NULL,
    -- 用户编号：不允许为空，通过外键关联 user 表，标识提交申请的账号。
    user_id BIGINT NOT NULL,
    -- 联系电话：最长 30 个字符且不允许为空，供审核及领养沟通使用。
    contact_phone VARCHAR(30) NOT NULL,
    -- 联系地址：最长 255 个字符且不允许为空，记录申请人的居住或联系地址。
    address VARCHAR(255) NOT NULL,
    -- 住房条件：最长 1000 个字符且不允许为空，用于说明居住空间及饲养环境。
    housing_condition VARCHAR(1000) NOT NULL,
    -- 养宠经验：最长 1000 个字符，允许为空，用于补充既往饲养经历。
    experience VARCHAR(1000),
    -- 申请理由：最长 1000 个字符且不允许为空，用于说明领养动机及陪伴计划。
    reason VARCHAR(1000) NOT NULL,
    -- 审核状态：最长 20 个字符且不允许为空，默认 PENDING 表示等待审核。
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- 审核意见：最长 1000 个字符，允许为空，由管理员在审核时填写。
    review_comment VARCHAR(1000),
    -- 申请提交时间：不允许为空，插入记录时默认使用数据库当前时间。
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 审核完成时间：允许为空，未审核时保存 NULL，审核后记录实际处理时间。
    reviewed_at TIMESTAMP NULL,
    -- 宠物外键 fk_application_pet：引用 pet.id；存在申请记录时 RESTRICT 阻止删除对应宠物。
    CONSTRAINT fk_application_pet FOREIGN KEY (pet_id) REFERENCES pet (id) ON DELETE RESTRICT,
    -- 用户外键 fk_application_user：引用 user.id；存在申请记录时 RESTRICT 阻止删除对应用户。
    CONSTRAINT fk_application_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE RESTRICT,
    -- 状态检查约束 chk_application_status：仅允许 PENDING（待审核）、APPROVED（已通过）、REJECTED（已拒绝）。
    CONSTRAINT chk_application_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    -- 联合索引 idx_application_pet_status：加速查询指定宠物在特定审核状态下的申请。
    INDEX idx_application_pet_status (pet_id, status),
    -- 联合索引 idx_application_user_created：加速按用户查询申请历史，并按提交时间排序。
    INDEX idx_application_user_created (user_id, created_at),
    -- 联合索引 idx_application_status_created：加速后台按审核状态筛选申请，并按提交时间排序。
    INDEX idx_application_status_created (status, created_at)
    -- 使用 InnoDB 存储引擎，为申请数据提供事务、外键及索引支持。
) ENGINE = InnoDB;

-- 向角色表写入两个固定角色，显式指定 id 和 role_name，便于后续稳定建立用户角色关联。
INSERT INTO `role` (id, role_name) VALUES
    -- 角色编号 1：ROLE_USER，表示普通平台用户角色。
    (1, 'ROLE_USER'),
    -- 角色编号 2：ROLE_ADMIN，表示拥有后台管理权限的管理员角色。
    (2, 'ROLE_ADMIN');

-- 测试账号密码：管理员为 admin/admin123，普通用户为 user/user123；password 字段保存已生成并验证的 BCrypt 哈希。
-- 插入用户种子数据，显式指定用户编号、用户名、BCrypt 密码哈希、邮箱、电话、头像及启用状态。
INSERT INTO `user` (id, username, password, email, phone, avatar, enabled) VALUES
    -- 用户编号 1：管理员 admin，邮箱 admin@petadoption.local，电话 13800000001，未设置头像，账号已启用。
    (1, 'admin', '$2b$10$IdtOiZDGwwvsaEDR8OSyWu331Vyb7xhGc7lRWNsQPaDIRem0g8iY2', 'admin@petadoption.local', '13800000001', NULL, TRUE),
    -- 用户编号 2：普通用户 user，邮箱 user@petadoption.local，电话 13800000002，未设置头像，账号已启用。
    (2, 'user', '$2b$10$WW3WoPemkYK2uckS0pMdkuSs3OKEQESz3mOTDf1.ZsakCZ4uIrh/a', 'user@petadoption.local', '13800000002', NULL, TRUE);

-- 写入用户角色关联种子数据，为上述测试账号分配对应角色。
INSERT INTO user_role (user_id, role_id) VALUES
    -- 将用户编号 1 的 admin 关联到角色编号 2 的 ROLE_ADMIN，授予管理员权限。
    (1, 2),
    -- 将用户编号 2 的 user 关联到角色编号 1 的 ROLE_USER，授予普通用户权限。
    (2, 1);

-- 写入品种种子数据，显式指定品种编号、名称、物种及品种介绍。
INSERT INTO breed (id, name, species, description) VALUES
    -- 品种编号 1：英国短毛猫，物种为猫，特点为温和安静、适应能力强且被毛浓密。
    (1, '英国短毛猫', '猫', '性格温和安静，适应能力强，被毛浓密。'),
    -- 品种编号 2：暹罗猫，物种为猫，特点为聪明亲人、活泼好奇且喜欢互动。
    (2, '暹罗猫', '猫', '聪明亲人，活泼好奇，喜欢与主人互动。'),
    -- 品种编号 3：柯基犬，物种为犬，特点为开朗友善、精力充沛，适合能够规律陪伴的家庭。
    (3, '柯基犬', '犬', '开朗友善，精力充沛，适合有规律陪伴的家庭。'),
    -- 品种编号 4：金毛寻回犬，物种为犬，特点为温顺聪明、耐心友好，需要充足运动。
    (4, '金毛寻回犬', '犬', '温顺聪明，耐心友好，需要充足运动。'),
    -- 品种编号 5：柴犬，物种为犬，特点为独立机警、忠诚爱干净，需要稳定的社会化训练。
    (5, '柴犬', '犬', '独立机警，忠诚爱干净，需要稳定的社会化训练。'),
    -- 品种编号 6：西伯利亚哈士奇，物种为犬，活泼外向且运动量较大，适合时间充裕的领养人。
    (6, '西伯利亚哈士奇', '犬', '活泼外向，运动量较大，适合时间充裕的领养人。'),
    -- 品种编号 7：贵宾犬，物种为犬，聪慧亲人、学习能力强，需要定期梳理被毛。
    (7, '贵宾犬', '犬', '聪慧亲人，学习能力强，需要定期梳理被毛。');

-- 写入宠物种子数据，依次设置宠物编号、名称、品种编号、性别、月龄、健康状况、介绍及领养状态。
INSERT INTO pet (id, name, breed_id, gender, age_months, health_status, description, status) VALUES
    -- 宠物编号 1：糯米，英国短毛猫，雌性，18 月龄，已接种疫苗并绝育，安静亲人，当前可领养。
    (1, '糯米', 1, '雌性', 18, '已接种疫苗，已绝育', '性格安静亲人，喜欢趴在窗边晒太阳，能够适应室内家庭生活。', 'AVAILABLE'),
    -- 宠物编号 2：豆豆，暹罗猫，雄性，14 月龄，已接种疫苗并驱虫，活泼好奇，当前可领养。
    (2, '豆豆', 2, '雄性', 14, '已接种疫苗，已驱虫', '活泼好奇，喜欢互动玩具，希望领养人每天能安排陪伴时间。', 'AVAILABLE'),
    -- 宠物编号 3：布丁，柯基犬，雄性，20 月龄，已接种疫苗且体检健康，友善外向，当前可领养。
    (3, '布丁', 3, '雄性', 20, '已接种疫苗，体检健康', '友善外向，喜欢散步，已经掌握基本随行和定点如厕。', 'AVAILABLE'),
    -- 宠物编号 4：阳光，金毛寻回犬，雌性，26 月龄，已接种疫苗并绝育，温柔耐心，当前可领养。
    (4, '阳光', 4, '雌性', 26, '已接种疫苗，已绝育', '温柔耐心，对孩子和其他宠物友好，需要每天适量运动。', 'AVAILABLE'),
    -- 宠物编号 5：小枫，柴犬，雄性，16 月龄，已接种疫苗并驱虫，聪明独立，当前可领养。
    (5, '小枫', 5, '雄性', 16, '已接种疫苗，已驱虫', '聪明独立，熟悉基础口令，适合愿意持续训练的家庭。', 'AVAILABLE'),
    -- 宠物编号 6：雪花，西伯利亚哈士奇，雌性，22 月龄，已接种疫苗且体检健康，精力旺盛，当前可领养。
    (6, '雪花', 6, '雌性', 22, '已接种疫苗，体检健康', '精力旺盛，喜欢户外活动，需要安全的活动空间和充足陪伴。', 'AVAILABLE'),
    -- 宠物编号 7：可可，贵宾犬，雄性，12 月龄，已接种疫苗并驱虫，亲人聪明，当前可领养。
    (7, '可可', 7, '雄性', 12, '已接种疫苗，已驱虫', '亲人聪明，学习指令很快，需要定期美容和温和引导。', 'AVAILABLE');

-- 写入宠物图片种子数据，为每只宠物指定图片访问路径，并将这些图片设为封面。
INSERT INTO pet_image (pet_id, image_path, cover) VALUES
    -- 宠物编号 1（糯米）的封面图片：英国短毛猫图片路径，cover 为 TRUE。
    (1, '/images/products/british-shorthair.jpg', TRUE),
    -- 宠物编号 2（豆豆）的封面图片：暹罗猫图片路径，cover 为 TRUE。
    (2, '/images/products/siamese.jpg', TRUE),
    -- 宠物编号 3（布丁）的封面图片：柯基犬图片路径，cover 为 TRUE。
    (3, '/images/products/corgi.jpg', TRUE),
    -- 宠物编号 4（阳光）的封面图片：金毛寻回犬图片路径，cover 为 TRUE。
    (4, '/images/products/golden-retriever.jpg', TRUE),
    -- 宠物编号 5（小枫）的封面图片：柴犬图片路径，cover 为 TRUE。
    (5, '/images/products/shiba-inu.jpg', TRUE),
    -- 宠物编号 6（雪花）的封面图片：哈士奇图片路径，cover 为 TRUE。
    (6, '/images/products/husky.jpg', TRUE),
    -- 宠物编号 7（可可）的封面图片：贵宾犬图片路径，cover 为 TRUE。
    (7, '/images/products/poodle.jpg', TRUE);
