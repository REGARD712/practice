// 首页：餐厅品牌 + 分类快捷入口 + 热门推荐 + 招牌菜品
const app = getApp();
const config = require('../../utils/config');
const api = require('../../utils/api');
const { resolveList } = require('../../utils/image');
const { fenToYuan } = require('../../utils/format');
const cartUtil = require('../../utils/cart');

Page({
  data: {
    restaurant: {
      name: config.RESTAURANT_NAME,
      slogan: config.RESTAURANT_SLOGAN,
      hours: config.RESTAURANT_HOURS
    },
    notice: config.NOTICE,
    categories: [],      // 分类快捷入口
    hotDishes: [],       // 热门推荐（按销量）
    signatureDishes: [], // 招牌菜品
    userInfo: null,
    cartCount: 0,
    loading: true
  },

  onShow() {
    this.syncUserInfo();
    this.setData({ cartCount: cartUtil.getTotals().count });
    this.loadMenu();
  },

  // 启动时全局登录态可能仍在恢复中，就绪后再取用户信息
  syncUserInfo() {
    const ready = app.userStateReady || Promise.resolve();
    ready.then(() => {
      this.setData({ userInfo: app.globalData.userInfo });
    });
  },

  // 加载菜单（云端 categories + dishes，公共只读表，无需登录），与点单页共享缓存
  async loadMenu(force = false) {
    if (!force && app.globalData.menuCache) {
      this.renderMenu(app.globalData.menuCache);
      return;
    }
    this.setData({ loading: true });
    try {
      const menu = await api.getMenu();
      app.globalData.menuCache = menu;
      this.renderMenu(menu);
    } catch (err) {
      // 加载失败：保留已有内容，下拉可重试
      this.setData({ loading: false });
      wx.showToast({ title: '菜单加载失败，请下拉重试', icon: 'none' });
    }
  },

  async renderMenu(menu) {
    const dishes = menu.dishes || [];
    const hot = dishes.slice().sort((a, b) => (b.sales || 0) - (a.sales || 0)).slice(0, 6);
    const signature = dishes.filter((d) => (d.tags || []).indexOf('招牌') >= 0).slice(0, 4);
    const [hotList, sigList] = await Promise.all([
      resolveList(hot),
      resolveList(signature.length ? signature : dishes.slice(0, 4))
    ]);
    this.setData({
      // 分类快捷入口：预计算首字与菜品的展示价格
      categories: (menu.categories || []).map((c) => ({ ...c, firstChar: c.name.slice(0, 1) })),
      hotDishes: hotList.map((d) => ({ ...d, priceYuan: fenToYuan(d.price) })),
      signatureDishes: sigList.map((d) => ({ ...d, priceYuan: fenToYuan(d.price) })),
      loading: false
    });
  },

  // 分类快捷入口 → 点单页对应分类
  onCategoryTap(e) {
    const { id } = e.currentTarget.dataset;
    // switchTab 不能带参数，通过全局变量传递目标分类
    app.globalData.pendingCategoryId = id;
    wx.switchTab({ url: '/pages/menu/menu' });
  },

  // 去点单
  goMenu() {
    wx.switchTab({ url: '/pages/menu/menu' });
  },

  // 菜品 → 详情页
  onDishTap(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: '/pages/detail/detail?id=' + id });
  },

  // 快速加购（首页推荐区）
  onQuickAdd(e) {
    const { id } = e.currentTarget.dataset;
    const dish = this.data.signatureDishes.find((d) => d._id === id) ||
      this.data.hotDishes.find((d) => d._id === id);
    if (!dish) return;
    cartUtil.addToCart({ id: dish._id, name: dish.name, price: dish.price, image: dish.image }, 1);
    this.setData({ cartCount: cartUtil.getTotals().count });
    wx.showToast({ title: '已加入购物车', icon: 'none' });
  },

  // 登录入口
  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' });
  },

  onPullDownRefresh() {
    this.loadMenu(true).finally(() => wx.stopPullDownRefresh());
  }
});
