CREATE DATABASE IF NOT EXISTS itnews_db DEFAULT CHARSET utf8mb4;

USE itnews_db;
-- 用户表（仅作为作者参考数据）

CREATE TABLE `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 新闻分类表
CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 新闻表
CREATE TABLE article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL, -- 作者ID
    category_id BIGINT NOT NULL, -- 分类ID
    FOREIGN KEY (user_id) REFERENCES user (id),
    FOREIGN KEY (category_id) REFERENCES category (id)
);
-- 插入测试数据（作者）
INSERT INTO user (username) VALUES ('张三'), ('李四'), ('王五');
-- 插入分类测试数据
INSERT INTO category (name) VALUES ('科技'), ('生活'), ('教育');
-- 插入新闻测试数据
INSERT INTO
    article (
        title,
        content,
        user_id,
        category_id
    )
VALUES (
        '再添一员！泰国加入国际月球科研站计划',
        '4月5日，中国国家航天局与泰国高等教育科研与创新部共同签署关于
探索与和平利用外层空间合作的谅解备忘录和国际月球科研站合作的谅解备忘录。',
        1,
        1
    ),
    (
        '我国科学家研发新型通用晶体生长技术',
        '华东理工团队自主研发了一种钙钛矿单晶晶片通用生长技术，将晶体生长
周期由7天缩短至1.5天，实现了30余种金属卤化物钙钛矿半导体的可控制备。',
        2,
        1
    ),
    (
        'OpenAI正式发布GPT-4人工智能大模型',
        'OpenAI发布了多模态预训练大模型GPT-4，这是其大型语言模型的最
新版本。与此前的版本相比，GPT-4具备强大的各面向能力，GPT-4的发布是人工智能应用的一个里程碑事件。',
        1,
        1
    );