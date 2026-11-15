# pet-mgt 宠物领养管理平台

基于 Spring Boot 的宠物领养管理平台，采用 Thymeleaf 服务端渲染页面，提供宠物展示、用户注册登录、领养申请、申请审核以及后台管理等功能。系统区分普通用户和管理员两类角色，适用于 Java Web 单体应用学习与实验。

## 功能特性

### 访客

- 查看首页推荐宠物及平台统计数据
- 浏览宠物列表和宠物详情
- 按关键词、品种筛选宠物，并分页查看结果
- 注册普通用户账号

### 登录用户

- 通过表单登录和安全退出
- 填写联系方式、住房条件、养宠经验及申请理由，提交领养申请
- 查看本人全部领养申请及审核进度

### 管理员

- 查看后台仪表盘、宠物与申请统计以及近期申请
- 新增、编辑、删除宠物资料，上传多张宠物图片
- 按状态筛选领养申请，执行通过或拒绝审核并填写审核意见
- 新增、编辑、删除用户并分配用户角色

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 开发语言 | Java 17 |
| 核心框架 | Spring Boot 3.3.5、Spring MVC |
| 页面渲染 | Thymeleaf、HTML、CSS、JavaScript |
| 安全认证 | Spring Security、BCrypt |
| 数据访问 | MyBatis 3.0.3 |
| 数据库 | MySQL |
| 数据校验 | Jakarta Bean Validation |
| 构建工具 | Maven |
| 其他 | Lombok |

## 项目结构

```text
pet-mgt/
├── db/
│   └── script.sql                    # 数据库建表及初始化数据
├── src/
│   ├── main/
│   │   ├── java/com/petadoption/
│   │   │   ├── config/               # Spring Security、MVC 与认证配置
│   │   │   ├── controller/           # 公开页面、用户申请及后台控制器
│   │   │   ├── dto/                  # 注册和用户管理表单对象
│   │   │   ├── entity/               # 用户、宠物、申请等实体
│   │   │   ├── exception/            # 业务异常与全局异常处理
│   │   │   ├── mapper/               # MyBatis 数据访问接口
│   │   │   ├── service/              # 核心业务与文件存储服务
│   │   │   └── PetManagementApplication.java
│   │   └── resources/
│   │       ├── static/               # CSS、JavaScript 和静态图片
│   │       ├── templates/            # Thymeleaf 页面模板
│   │       └── application.yml       # 应用与环境变量配置
│   └── test/                         # 测试源码目录
├── pom.xml
└── README.md
```

## 环境要求

- JDK 17 或更高版本
- Maven 3.8 或更高版本
- MySQL 8.0 或更高版本

## 快速开始

### 1. 获取代码

当前项目位于 Gitee 仓库的 `javaweb/pet-mgt` 目录：

```bash
git clone https://gitee.com/rae-gard712/practice.git
cd practice/javaweb/pet-mgt
```

### 2. 初始化数据库

使用 MySQL 客户端执行初始化脚本：

```bash
mysql -u root -p
```

进入 MySQL 客户端后执行：

```sql
source db/script.sql;
```

脚本会创建 `pet_adoption` 数据库、7 张业务表以及演示数据。

> **注意：** 初始化脚本会删除并重建 `pet_adoption` 数据库中的业务表，请勿直接用于存在重要数据的环境。

### 3. 配置环境变量

数据库账号和密码支持通过环境变量配置。Windows PowerShell 示例：

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="pet_adoption"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="请替换为你的数据库密码"
```

以上变量仅在当前 PowerShell 会话中生效。也可以直接修改 `src/main/resources/application.yml` 中的本地开发配置，但请勿将真实密码提交到仓库。

### 4. 启动应用

```bash
mvn spring-boot:run
```

启动成功后访问：<http://localhost:8080>

## 演示账号

初始化脚本提供以下本地演示账号：

| 角色 | 用户名 | 密码 | 权限 |
| --- | --- | --- | --- |
| 管理员 | `admin` | `admin123` | 后台仪表盘、宠物管理、申请审核、用户管理 |
| 普通用户 | `user` | `user123` | 提交领养申请、查看本人申请 |

这些账号仅用于本地开发和演示，部署到公开环境前请修改或删除默认账号。

## 配置说明

主要配置位于 `src/main/resources/application.yml`。

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `DB_HOST` | `localhost` | MySQL 主机地址 |
| `DB_PORT` | `3306` | MySQL 端口 |
| `DB_NAME` | `pet_adoption` | 数据库名称 |
| `MYSQL_USERNAME` | `root` | 数据库用户名 |
| `MYSQL_PASSWORD` | 按本地环境设置 | 数据库密码，建议显式配置 |
| `SERVER_PORT` | `8080` | 应用监听端口 |
| `UPLOAD_DIR` | `uploads/pets` | 宠物图片上传目录 |

上传目录会在应用启动时自动创建，并通过 `/uploads/**` 地址对外提供访问。单个上传文件最大为 2 MB，单次上传请求最大为 10 MB。

## 主要页面路由

| 路径 | 访问权限 | 说明 |
| --- | --- | --- |
| `/` | 公开 | 平台首页 |
| `/pets` | 公开 | 宠物列表、搜索、筛选及分页 |
| `/pets/{id}` | 公开 | 宠物详情 |
| `/login` | 公开 | 登录页面 |
| `/register` | 公开 | 普通用户注册 |
| `/pets/{id}/apply` | 登录用户 | 提交领养申请 |
| `/applications/mine` | 登录用户 | 我的领养申请 |
| `/admin` | 管理员 | 后台仪表盘 |
| `/admin/pets` | 管理员 | 宠物管理 |
| `/admin/applications` | 管理员 | 领养申请审核 |
| `/admin/users` | 管理员 | 用户及角色管理 |

## 领养状态流转

宠物状态与申请状态在同一事务中联动：

```text
用户提交申请：宠物 AVAILABLE → PENDING，申请创建为 PENDING
管理员审核通过：申请 PENDING → APPROVED，宠物 → ADOPTED
管理员审核拒绝：申请 PENDING → REJECTED，宠物恢复为 AVAILABLE
```

系统通过事务、条件更新和行锁防止重复申请与重复审核。

## 数据库表

| 表名 | 用途 |
| --- | --- |
| `role` | 角色定义 |
| `user` | 用户账号、密码摘要及联系方式 |
| `user_role` | 用户与角色的多对多关联 |
| `breed` | 宠物品种资料 |
| `pet` | 宠物档案及领养状态 |
| `pet_image` | 宠物图片及封面标记 |
| `adoption_application` | 领养申请、审核结果及审核意见 |

## 构建与运行 JAR

```bash
mvn clean package
java -jar target/pet-mgt-1.0.0.jar
```

运行测试或校验测试编译：

```bash
mvn test
```

## 安全说明

- 用户密码使用 BCrypt 加密存储，不保存明文密码。
- `/admin/**` 仅允许具有 `ROLE_ADMIN` 角色的用户访问。
- 领养申请与当前登录会话绑定，用户只能查看本人的申请记录。
- 写操作使用 POST 请求，并保留 Spring Security 默认的 CSRF 防护。
- 数据库凭据应通过环境变量或本地未跟踪配置注入，不应提交真实密码。
