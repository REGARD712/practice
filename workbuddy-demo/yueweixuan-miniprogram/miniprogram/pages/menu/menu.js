// 点单页：左侧分类栏 + 右侧菜品列表 + 底部购物车栏
const app = getApp();
const api = require('../../utils/api');
const { resolveList } = require('../../utils/image');
const { fenToYuan } = require('../../utils/format');
const cartUtil = require('../../utils/cart');

Page({
  data: {
    categories: [],
    activeIndex: 0,
    dishList: [],       // 当前分类下的菜品（已合并购物车数量）
    cartCount: 0,
    cartAmountYuan: '0.00',
    loading: true
  },

  allDishes: [], // 全量菜品缓存

  onLoad(options) {
    // 首页分类入口跳转：switchTab 无法带参，通过全局变量传递
    this.pendingCategoryId = app.globalData.pendingCategoryId || options.categoryId || '';
    app.globalData.pendingCategoryId = '';
  },

  onShow() {
    this.loadMenu();
  },

  // 加载菜单（云端 categories + dishes，公共只读表，无需登录）
  async loadMenu(force = false) {
    let menu = app.globalData.menuCache;
    if (force || !menu) {
      try {
        menu = await api.getMenu();
        app.globalData.menuCache = menu;
      } catch (err) {
        // 加载失败：保留已有内容，下拉可重试
        this.setData({ loading: false });
        wx.showToast({ title: '菜单加载失败，请下拉重试', icon: 'none' });
        return;
      }
    }

    this.allDishes = menu.dishes || [];
    const categories = (menu.categories || []).map((c) => ({ ...c, firstChar: c.name.slice(0, 1) }));

    // 定位到指定分类（首页入口跳转）
    let activeIndex = 0;
    if (this.pendingCategoryId) {
      const idx = categories.findIndex((c) => c._id === this.pendingCategoryId);
      if (idx >= 0) activeIndex = idx;
      this.pendingCategoryId = '';
    }

    this.setData({ categories, activeIndex, loading: false });
    this.renderDishList();
  },

  // 渲染当前分类菜品（合并购物车数量与展示价格）
  async renderDishList() {
    const cat = this.data.categories[this.data.activeIndex];
    if (!cat) return;
    let list = this.allDishes.filter((d) => d.categoryId === cat._id);
    // 云端已按 sort 排序，此处保持
    list = list.slice().sort((a, b) => (a.sort || 0) - (b.sort || 0));

    const cartMap = {};
    cartUtil.getCart().forEach((it) => { cartMap[it.id] = it.quantity; });

    const resolved = await resolveList(list);
    this.setData({
      dishList: resolved.map((d) => ({
        ...d,
        priceYuan: fenToYuan(d.price),
        quantity: cartMap[d._id] || 0
      }))
    });
    this.refreshCartBar();
  },

  // 切换分类
  onCategoryTap(e) {
    const index = Number(e.currentTarget.dataset.index);
    if (index === this.data.activeIndex) return;
    this.setData({ activeIndex: index });
    this.renderDishList();
  },

  // 加购（+ 按钮）
  onAdd(e) {
    const { id } = e.currentTarget.dataset;
    const dish = this.data.dishList.find((d) => d._id === id);
    if (!dish) return;
    cartUtil.addToCart({ id: dish._id, name: dish.name, price: dish.price, image: dish.image }, 1);
    this.syncDishQuantity(id, 1);
    this.refreshCartBar();
  },

  // 减购（- 按钮）
  onMinus(e) {
    const { id } = e.currentTarget.dataset;
    const dish = this.data.dishList.find((d) => d._id === id);
    if (!dish) return;
    const next = (dish.quantity || 0) - 1;
    cartUtil.updateQuantity(dish._id, next);
    this.syncDishQuantity(id, next - (dish.quantity || 0));
    this.refreshCartBar();
  },

  // 只更新受影响菜品的数量字段，避免整列表 setData
  syncDishQuantity(id, delta) {
    const idx = this.data.dishList.findIndex((d) => d._id === id);
    if (idx < 0) return;
    const key = `dishList[${idx}].quantity`;
    this.setData({ [key]: this.data.dishList[idx].quantity + delta });
  },

  refreshCartBar() {
    const { count, amount } = cartUtil.getTotals();
    this.setData({ cartCount: count, cartAmountYuan: fenToYuan(amount) });
  },

  onDishTap(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: '/pages/detail/detail?id=' + id });
  },

  // 底部购物车栏 → 购物车页
  goCart() {
    wx.switchTab({ url: '/pages/cart/cart' });
  },

  onPullDownRefresh() {
    this.loadMenu(true).finally(() => wx.stopPullDownRefresh());
  }
});
