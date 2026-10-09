-- =====================================================================
-- 悦味轩点单小程序 · 云数据库结构存档（WorkBuddy Cloud Service）
-- 应用：wbapp_x0e7jgJPb6FoLyANc7n2Xs
--
-- 本文件为已执行 DDL / 表级授权 / 行级安全策略 / 种子数据的存档记录，
-- 由管理面（migrate，管理员角色）执行；小程序端只做 DML，永不执行 DDL。
--
-- 安全模型（两道门）：
--   1) 表级 GRANT —— anon（游客）与 authenticated（登录用户）的库级权限
--   2) 行级策略 RLS —— 真正的访问边界：
--      categories / dishes 仅有 SELECT 策略 => 实际只读（写入被拒）
--      user_profiles / orders 按 owner_id = auth.uid() 隔离，跨用户读写一律拒绝
--
-- 约定：
--   - 金额一律以「分」（INTEGER）存储，展示层再转元
--   - dishes.image 为小程序内置静态资源路径（/assets/images/...，随包分发）
--   - user_profiles.avatar 为云存储对象路径（users/<uid>/avatars/...）
--   - owner_id 由 DEFAULT auth.uid() 填充，客户端不传该字段
-- =====================================================================

-- ---------- 表结构 ----------

CREATE TABLE categories (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  key TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL,
  sort INTEGER NOT NULL DEFAULT 0,
  color TEXT NOT NULL DEFAULT '#e8552f',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE categories IS '菜品分类（公共只读）';

CREATE TABLE dishes (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  category_id BIGINT NOT NULL REFERENCES categories(id),
  name TEXT NOT NULL,
  description TEXT NOT NULL DEFAULT '',
  price INTEGER NOT NULL,                 -- 单位：分
  image TEXT NOT NULL DEFAULT '',         -- 小程序内置静态资源路径
  sales INTEGER NOT NULL DEFAULT 0,
  tags TEXT[] NOT NULL DEFAULT '{}',
  status TEXT NOT NULL DEFAULT 'on_sale', -- on_sale / off_sale
  sort INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE dishes IS '菜品（公共只读；下单时以本表现价重算金额）';

CREATE TABLE user_profiles (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  owner_id TEXT NOT NULL DEFAULT auth.uid(),
  nickname TEXT NOT NULL DEFAULT '',
  avatar TEXT NOT NULL DEFAULT '',        -- 云存储对象路径 users/<uid>/avatars/...
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE user_profiles IS '用户资料（仅本人读写）';

CREATE TABLE orders (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  owner_id TEXT NOT NULL DEFAULT auth.uid(),
  order_no TEXT NOT NULL,
  items JSONB NOT NULL,                   -- [{ dishId, name, price, image, quantity }]
  total_amount INTEGER NOT NULL,          -- 单位：分
  meal_type TEXT NOT NULL,                -- dine_in / takeout
  status TEXT NOT NULL DEFAULT 'placed',  -- placed / completed / cancelled
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE orders IS '订单（仅本人可读可建；金额按云端 dishes 现价计算）';

-- ---------- 行级安全（RLS） ----------

ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE dishes ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;

-- 目录表：所有人可读；未建写策略 => 写入被 RLS 拒绝（实际只读）
CREATE POLICY categories_read_all ON categories
  FOR SELECT TO authenticated, anon USING (true);

CREATE POLICY dishes_read_all ON dishes
  FOR SELECT TO authenticated, anon USING (true);

-- 用户资料：仅本人读 / 建 / 改
CREATE POLICY user_profiles_select_own ON user_profiles
  FOR SELECT TO authenticated, anon USING (owner_id = auth.uid());

CREATE POLICY user_profiles_insert_own ON user_profiles
  FOR INSERT TO authenticated, anon WITH CHECK (owner_id = auth.uid());

CREATE POLICY user_profiles_update_own ON user_profiles
  FOR UPDATE TO authenticated, anon
  USING (owner_id = auth.uid()) WITH CHECK (owner_id = auth.uid());

-- 订单：仅本人可读、可建（无更新/删除策略）
CREATE POLICY orders_select_own ON orders
  FOR SELECT TO authenticated, anon USING (owner_id = auth.uid());

CREATE POLICY orders_insert_own ON orders
  FOR INSERT TO authenticated, anon WITH CHECK (owner_id = auth.uid());

-- ---------- 表级授权（第一道门） ----------
-- 注：authenticated 的表级权限较宽，但 RLS 策略才是实际访问边界：
--     categories / dishes 只有 SELECT 策略，写入一律被拒。

GRANT ALL PRIVILEGES ON TABLE categories, dishes, user_profiles, orders TO authenticated;
GRANT SELECT ON categories TO anon;
GRANT SELECT ON dishes TO anon;
GRANT SELECT, INSERT, UPDATE ON user_profiles TO anon;
GRANT SELECT, INSERT ON orders TO anon;

-- ---------- 种子数据（菜单） ----------
-- 当前环境实际行 id：categories 1-5；dishes 2-14（重建环境时以序列自动生成为准）

INSERT INTO categories (key, name, sort, color) VALUES
  ('c1', '招牌推荐', 1, '#e8552f'),
  ('c2', '经典热菜', 2, '#f07a4a'),
  ('c3', '爽口凉菜', 3, '#f2a65a'),
  ('c4', '主食小吃', 4, '#7fb069'),
  ('c5', '饮品甜点', 5, '#5b8db8');

INSERT INTO dishes (category_id, name, description, price, image, sales, tags, status, sort) VALUES
  (1, '招牌红烧肉',   '精选五花三层，慢火细炖，肥而不腻，入口即化', 3800, '/assets/images/dish-1.png', 326, '{招牌,热销}', 'on_sale', 1),
  (1, '秘制香辣烤鱼', '鲜活草鱼现杀现烤，配秘制香辣料，麻辣鲜香', 5800, '/assets/images/dish-2.png', 289, '{招牌}',      'on_sale', 2),
  (2, '宫保鸡丁',     '鸡腿肉配花生米，酸甜微辣，经典下饭菜',     2800, '/assets/images/dish-3.png', 412, '{热销}',      'on_sale', 1),
  (2, '麻婆豆腐',     '嫩豆腐配牛肉末，麻辣烫香，酥嫩入味',       2200, '/assets/images/dish-4.png', 256, '{}',          'on_sale', 2),
  (2, '鱼香肉丝',     '笋丝木耳配里脊，咸甜酸辣，鱼香浓郁',       2600, '/assets/images/dish-5.png', 198, '{}',          'on_sale', 3),
  (2, '农家小炒肉',   '土猪肉配青辣椒，锅气十足，香辣开胃',       3000, '/assets/images/dish-6.png', 345, '{热销}',      'on_sale', 4),
  (3, '口水鸡',       '三黄鸡配红油料汁，麻辣爽口，冷吃更佳',     2400, '/assets/images/dish-1.png', 176, '{}',          'on_sale', 1),
  (3, '凉拌木耳',     '云耳焯水冰镇，爽脆清口，解腻首选',         1200, '/assets/images/dish-2.png', 134, '{}',          'on_sale', 2),
  (3, '蒜泥白肉',     '薄切五花肉卷黄瓜，蒜香浓郁，肥瘦相宜',     2600, '/assets/images/dish-3.png', 121, '{}',          'on_sale', 3),
  (4, '扬州炒饭',     '粒粒分明的蛋炒饭，火腿虾仁青豆，料足量大', 1800, '/assets/images/dish-4.png', 387, '{热销}',      'on_sale', 1),
  (4, '葱油拌面',     '现熬葱油，酱香浓郁，简简单单的好味道',     1500, '/assets/images/dish-5.png', 165, '{}',          'on_sale', 2),
  (5, '手工双皮奶',   '水牛奶炖制，奶皮厚实，入口滑嫩',           1200, '/assets/images/dish-1.png', 208, '{}',          'on_sale', 2),
  (5, '古法酸梅汤',   '乌梅山楂陈皮慢熬，酸甜生津，解辣解腻',     800,  '/assets/images/dish-6.png', 523, '{热销}',      'on_sale', 1);
