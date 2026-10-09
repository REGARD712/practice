// 云服务业务层：菜单 / 登录 / 资料 / 订单 / 头像上传
const { cloud } = require('./cloud');

// ---------- 菜单（categories / dishes 为公共只读表，无需登录） ----------
async function getMenu() {
  const [catRes, dishRes] = await Promise.all([
    cloud.database.from('categories')
      .select('id,key,name,sort,color')
      .order('sort', { ascending: true }),
    cloud.database.from('dishes')
      .select('id,category_id,name,description,price,image,sales,tags,status,sort')
      .eq('status', 'on_sale')
      .order('sort', { ascending: true })
      .limit(100)
  ]);
  if (catRes.error) throw catRes.error;
  if (dishRes.error) throw dishRes.error;

  // 映射为前端展示模型（字段名与页面约定保持一致）
  const categories = (catRes.data || []).map((c) => ({
    _id: String(c.id),
    name: c.name,
    sort: c.sort,
    color: c.color
  }));
  const dishes = (dishRes.data || []).map((d) => ({
    _id: String(d.id),
    categoryId: String(d.category_id),
    name: d.name,
    desc: d.description || '',
    price: d.price,           // 单位：分
    image: d.image,
    sales: d.sales,
    tags: d.tags || [],
    sort: d.sort
  }));
  return { categories, dishes };
}

// ---------- 登录态 ----------
async function getSession() {
  const { data, error } = await cloud.auth.getSession();
  if (error) return null;
  return data || null;
}

// 微信授权登录：wx.login 取 code，换取 WorkBuddy 会话
function signInWechat() {
  return new Promise((resolve, reject) => {
    wx.login({
      async success({ code }) {
        try {
          // 必须传当前运行时 appid（试用版与正式版是不同小程序，不可写死）
          const appid = wx.getAccountInfoSync().miniProgram.appId;
          const { data, error } = await cloud.auth.signInWithWechat(code, appid);
          if (error) {
            console.error('[WorkBuddy Cloud] login failed', JSON.stringify({
              stage: 'wechat-login-handler',
              message: error.message || '微信登录失败，请重试'
            }));
            reject(error);
            return;
          }
          resolve(data);
        } catch (err) {
          console.error('[WorkBuddy Cloud] login failed', JSON.stringify({
            stage: 'wechat-login-handler',
            message: err instanceof Error ? err.message : '微信登录失败，请重试'
          }));
          reject(err);
        }
      },
      fail(err) {
        console.error('[WorkBuddy Cloud] login failed', JSON.stringify({
          stage: 'wx.login',
          message: err.errMsg
        }));
        reject(err);
      }
    });
  });
}

// ---------- 用户资料（user_profiles 表，仅本人可读写） ----------
async function getProfile() {
  // RLS 已限定仅本人可见；order + limit 保证 maybeSingle 不会因多行报错
  const { data, error } = await cloud.database
    .from('user_profiles')
    .select('id,nickname,avatar')
    .order('id', { ascending: false })
    .limit(1)
    .maybeSingle();
  if (error) return null;
  return data || null;
}

async function saveProfile(profile) {
  const existing = await getProfile();
  const payload = {
    nickname: profile.nickname,
    avatar: profile.avatar,
    updated_at: new Date().toISOString()
  };
  if (existing && existing.id) {
    const { data, error } = await cloud.database
      .from('user_profiles')
      .update(payload)
      .eq('id', existing.id)
      .select();
    if (error) throw error;
    // 空数组 = RLS 拦截（行非本人）或行不存在
    if (!data || data.length === 0) throw new Error('SAVE_BLOCKED');
    return data[0];
  }
  const { data, error } = await cloud.database
    .from('user_profiles')
    .insert(payload)
    .select();
  if (error) throw error;
  if (!data || data.length === 0) throw new Error('SAVE_BLOCKED');
  return data[0];
}

// ---------- 头像上传（云存储，所有者路径 users/<uid>/avatars/...） ----------
function readFileAsync(filePath) {
  return new Promise((resolve, reject) => {
    wx.getFileSystemManager().readFile({
      filePath,
      success: (res) => resolve(res.data), // ArrayBuffer
      fail: reject
    });
  });
}

async function uploadAvatar(userId, tempFilePath) {
  const buffer = await readFileAsync(tempFilePath);
  const path = cloud.storage.userPath(userId, 'avatars/avatar_' + Date.now() + '.jpg');
  const { error } = await cloud.storage.upload(path, buffer, {
    contentType: 'image/jpeg',
    metadata: { purpose: 'avatar' }
  });
  if (error) throw error;
  return path; // 写入 user_profiles.avatar 的对象路径
}

// ---------- 订单（orders 表，仅本人可读可建；金额按云端菜品现价计算） ----------
async function createOrder(cartItems, mealType) {
  const session = await getSession();
  if (!session) return { error: 'login-required' };

  // 从云端读取菜品现价，前端购物车价格不作为计价依据
  const ids = cartItems.map((it) => Number(it.id));
  const { data: dishRows, error: dishErr } = await cloud.database
    .from('dishes')
    .select('id,name,price,image,status')
    .in('id', ids);
  if (dishErr) throw dishErr;

  const dishMap = {};
  (dishRows || []).forEach((d) => { dishMap[String(d.id)] = d; });

  let totalAmount = 0;
  const items = [];
  for (const it of cartItems) {
    const d = dishMap[String(it.id)];
    if (!d || d.status !== 'on_sale') {
      return { error: '部分菜品已下架，请返回购物车调整' };
    }
    totalAmount += d.price * it.quantity;
    items.push({
      dishId: d.id,
      name: d.name,
      price: d.price,
      image: d.image,
      quantity: it.quantity
    });
  }

  const orderNo = 'ORD' + Date.now() + Math.random().toString(36).slice(2, 8).toUpperCase();
  const { data: inserted, error } = await cloud.database
    .from('orders')
    .insert({
      order_no: orderNo,
      items,              // JSONB
      total_amount: totalAmount,
      meal_type: mealType,
      status: 'placed'
    })
    .select();
  if (error) {
    if (error.code === '42501') return { error: '登录状态已失效，请重新登录' };
    throw error;
  }
  if (!inserted || inserted.length === 0) return { error: '下单失败，请重试' };
  return { data: { orderNo, totalAmount } };
}

async function listOrders() {
  const session = await getSession();
  if (!session) return { error: 'login-required' };

  const { data, error } = await cloud.database
    .from('orders')
    .select('order_no,items,total_amount,meal_type,status,created_at')
    .order('created_at', { ascending: false })
    .limit(20);
  if (error) {
    if (error.code === '42501') return { error: '登录状态已失效，请重新登录' };
    throw error;
  }
  const list = (data || []).map((o) => ({
    orderNo: o.order_no,
    items: o.items || [],
    totalAmount: o.total_amount,
    mealType: o.meal_type,
    status: o.status,
    createdAt: o.created_at
  }));
  return { data: list };
}

module.exports = {
  getMenu,
  getSession,
  signInWechat,
  getProfile,
  saveProfile,
  uploadAvatar,
  createOrder,
  listOrders
};
