// 订单记录页：按登录用户拉取云端订单（RLS 仅返回本人订单）
const config = require('../../utils/config');
const api = require('../../utils/api');
const { resolveList } = require('../../utils/image');
const { fenToYuan, formatTime } = require('../../utils/format');

Page({
  data: {
    list: [],       // 订单卡片视图数据
    loading: true,
    isEmpty: false,
    needLogin: false
  },

  onShow() {
    this.loadOrders();
  },

  async loadOrders() {
    this.setData({ loading: true });
    // 登录门：订单为用户私有数据，未登录展示登录引导
    const session = await api.getSession();
    if (!session) {
      this.setData({ loading: false, needLogin: true, isEmpty: false, list: [] });
      return;
    }
    try {
      const result = await api.listOrders();
      if (result.error) {
        // 会话失效（如 42501 权限拒绝）：提示并按未登录处理
        wx.showToast({ title: result.error, icon: 'none' });
        this.setData({ loading: false, needLogin: true, list: [] });
        return;
      }
      const orders = (result && result.data) || [];
      const list = [];
      for (const order of orders) {
        // 解析订单内菜品图片
        const items = await resolveList(order.items || []);
        list.push({
          orderNo: order.orderNo,
          statusText: config.ORDER_STATUS_TEXT[order.status] || order.status,
          mealTypeText: order.mealType === 'dine_in' ? '堂食' : '打包带走',
          items,
          totalAmountYuan: fenToYuan(order.totalAmount),
          timeText: formatTime(order.createdAt)
        });
      }
      this.setData({ list, loading: false, isEmpty: list.length === 0, needLogin: false });
    } catch (err) {
      // 网络异常等未预期失败
      console.error('[orders] load failed:', err && err.message);
      wx.showToast({ title: '订单加载失败，请下拉重试', icon: 'none' });
      this.setData({ loading: false, isEmpty: this.data.list.length === 0 });
    }
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' });
  },

  goMenu() {
    wx.switchTab({ url: '/pages/menu/menu' });
  },

  onPullDownRefresh() {
    this.loadOrders().finally(() => wx.stopPullDownRefresh());
  }
});
