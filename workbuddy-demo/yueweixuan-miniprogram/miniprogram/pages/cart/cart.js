// 购物车页：列表管理 + 结算下单
const app = getApp();
const api = require('../../utils/api');
const { resolveList } = require('../../utils/image');
const { fenToYuan } = require('../../utils/format');
const cartUtil = require('../../utils/cart');

Page({
  data: {
    items: [],          // [{ id, name, price, priceYuan, imageSrc, quantity }]
    totalCount: 0,
    totalAmountYuan: '0.00',
    submitting: false
  },

  onShow() {
    this.refresh();
  },

  async refresh() {
    const view = cartUtil.getCartView();
    const items = await resolveList(view.items);
    this.setData({
      items: items.map((it) => ({ ...it, priceYuan: fenToYuan(it.price) })),
      totalCount: view.totalCount,
      totalAmountYuan: fenToYuan(view.amount)
    });
  },

  onMinus(e) {
    const { id } = e.currentTarget.dataset;
    const item = this.data.items.find((it) => it.id === id);
    if (!item) return;
    cartUtil.updateQuantity(id, item.quantity - 1);
    this.refresh();
  },

  onAdd(e) {
    const { id } = e.currentTarget.dataset;
    const item = this.data.items.find((it) => it.id === id);
    if (!item) return;
    cartUtil.updateQuantity(id, item.quantity + 1);
    this.refresh();
  },

  onRemove(e) {
    const { id } = e.currentTarget.dataset;
    const that = this;
    wx.showModal({
      title: '移除菜品',
      content: '确定将该菜品移出购物车吗？',
      confirmColor: '#e8552f',
      success(res) {
        if (res.confirm) {
          cartUtil.removeItem(id);
          that.refresh();
        }
      }
    });
  },

  // 去结算：未登录先引导登录；已登录选择用餐方式后下单
  async onCheckout() {
    if (this.data.totalCount === 0 || this.data.submitting) return;

    // 登录门：订单为用户私有数据（RLS 保护），下单前需确认本人身份
    const session = await api.getSession();
    if (!session) {
      app.globalData.session = null;
      wx.showModal({
        title: '需要登录',
        content: '登录后才能保存订单记录，是否前往登录？',
        confirmColor: '#e8552f',
        success(res) {
          if (res.confirm) wx.navigateTo({ url: '/pages/login/login' });
        }
      });
      return;
    }
    app.globalData.session = session;

    const that = this;
    wx.showActionSheet({
      itemList: ['堂食', '打包带走'],
      async success(res) {
        await that.createOrder(res.tapIndex === 0 ? 'dine_in' : 'takeout');
      },
      fail() { /* 用户取消 */ }
    });
  },

  // 下单：金额按云端 dishes 现价重算（前端购物车价格不作为计价依据）
  async createOrder(mealType) {
    const cartItems = cartUtil.getCart().map((it) => ({ id: it.id, quantity: it.quantity }));
    this.setData({ submitting: true });
    try {
      const result = await api.createOrder(cartItems, mealType);
      if (result.error) {
        // 登录失效 / 菜品下架等业务失败，提示后留在当前页
        wx.showToast({ title: result.error, icon: 'none' });
        this.setData({ submitting: false });
        return;
      }
      cartUtil.clearCart();
      wx.showToast({ title: '下单成功', icon: 'success' });
      setTimeout(() => {
        wx.switchTab({ url: '/pages/orders/orders' });
      }, 600);
    } catch (err) {
      // 网络异常等未预期失败
      console.error('[cart] create order failed:', err && err.message);
      wx.showToast({ title: '下单失败，请重试', icon: 'none' });
      this.setData({ submitting: false });
    }
  },

  goMenu() {
    wx.switchTab({ url: '/pages/menu/menu' });
  }
});
