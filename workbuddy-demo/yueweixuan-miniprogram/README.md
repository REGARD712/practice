# 悦味轩点单小程序

餐饮点单微信小程序（原生开发 + WorkBuddy 云服务）。

## 功能清单

- 餐厅首页：品牌信息、公告、分类快捷入口、热门推荐、招牌菜品
- 点单页：左侧分类栏 + 右侧菜品列表、数量步进器、底部购物车栏
- 菜品详情：大图、价格、标签、介绍、数量选择、加入购物车
- 购物车：增删改查、合计、选择用餐方式（堂食/打包）后下单（未登录先引导登录）
- 订单记录：按登录用户拉取云端订单、下拉刷新（未登录展示登录引导）
- 微信一键登录：wx.login 换取 WorkBuddy 会话 + 头像昵称填写（官方推荐能力，头像存云存储）

## 技术栈与云服务

- 微信原生小程序（WXML / WXSS / JS），未使用第三方 UI 框架
- WorkBuddy 云服务（SDK：`@tencent-ai/workbuddy-cloud-sdk/miniprogram`）：
  - Database：categories / dishes（公共只读）、user_profiles / orders（RLS 按用户隔离）
  - Auth：微信小程序登录（`wx.login` → `signInWithWechat(code, appid)`）
  - Storage：用户头像上传（`users/<uid>/avatars/...`）与签名链接访问
- 金额以「分」存储；下单金额从云端 dishes 现价重算，前端购物车价格不作为计价依据
- 菜品图为小程序内置静态资源（随包分发、未登录也可加载）；用户头像为运行时文件，走云存储

## 目录结构

```
yueweixuan-miniprogram/
├── project.config.json          # 项目配置（appid 需替换）
├── cloudservice/schema.sql      # 云数据库结构存档（DDL/授权/RLS/种子数据）
├── miniprogram/                 # 小程序代码
│   ├── app.js / app.json / app.wxss
│   ├── package.json             # SDK 依赖（已构建 miniprogram_npm/）
│   ├── assets/                  # 图标与菜品占位图（tools/gen_icons.py 生成）
│   ├── components/empty-state/  # 空状态组件
│   ├── utils/                   # workbuddy-cloud-config(连接配置)/cloud(云客户端)/
│   │                            # api(业务层)/cart(购物车)/format/image/config(餐厅信息)
│   └── pages/                   # index 首页 / menu 点单 / detail 详情
│                                # cart 购物车 / orders 订单 / login 登录
└── tools/gen_icons.py           # 图标资源生成脚本
```

## 云服务配置

- 连接配置集中在 `miniprogram/utils/workbuddy-cloud-config.js`（endpoint + publishableKey，来自 WorkBuddy publicConfig，属可公开的前端配置，不含密钥）
- 数据库表结构、RLS 策略与种子数据见 `cloudservice/schema.sql`（已在云端执行，该文件为存档记录）
- 微信登录需应用完成小程序绑定且微信登录开关开启后方可使用

## 本地开发（可选）

1. 微信开发者工具导入本目录（appid 替换为自己的）
2. 已随仓库附带构建好的 `miniprogram_npm/`；如需重建：在 `miniprogram/` 下执行 `npm install`，再在开发者工具中「构建 npm」
3. 修改餐厅信息：`miniprogram/utils/config.js` 中集中配置餐厅名称、口号、营业时间、公告

## 合规提示（提审前必读，以微信官方最新规则为准）

- **类目资质**：餐饮点单类小程序需选择「餐饮」相关类目，通常需要《食品经营许可证》等资质（主体为餐厅本身时较易通过），以微信官方类目资质要求为准
- **隐私协议**：本项目收集头像与昵称、保存订单记录，需在微信公众平台「用户隐私保护指引」中声明相应收集类型
- **无强制登录**：浏览菜单、加购物车无需登录；仅下单与查看订单需登录，符合审核要求
- **支付**：当前版本为「下单记账」模式，未接入微信支付；后续接入需开通微信支付商户号
