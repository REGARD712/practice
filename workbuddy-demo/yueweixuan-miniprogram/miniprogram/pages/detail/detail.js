// 菜品详情页
const app = getApp();
const api = require('../../utils/api');
const { resolveImage } = require('../../utils/image');
const { fenToYuan } = require('../../utils/format');
const cartUtil = require('../../utils/cart');

Page({
  data: {
    dish: null,
    imageSrc: '',
    priceYuan: '',
    quantity: 1,
    loading: true
  },

  onLoad(options) {
    this.dishId = options.id || '';
    this.loadDish();
  },

  async loadDish() {
    // 优先使用菜单缓存，无缓存时从云端拉取
    let dish = null;
    const cache = app.globalData.menuCache;
    if (cache && cache.dishes) {
      dish = cache.dishes.find((d) => d._id === this.dishId);
    }
    if (!dish) {
      try {
        const menu = await api.getMenu();
        app.globalData.menuCache = menu;
        dish = (menu.dishes || []).find((d) => d._id === this.dishId);
      } catch (err) {
        this.setData({ loading: false });
        wx.showToast({ title: '加载失败，请稍后重试', icon: 'none' });
        return;
      }
    }

    if (!dish) {
      wx.showToast({ title: '菜品不存在或已下架', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 800);
      return;
    }

    const imageSrc = await resolveImage(dish.image);
    this.setData({
      dish,
      imageSrc,
      priceYuan: fenToYuan(dish.price),
      subtotalYuan: fenToYuan(dish.price),
      loading: false
    });
  },

  // 更新数量与小计
  updateQuantity(quantity) {
    this.setData({
      quantity,
      subtotalYuan: fenToYuan(this.data.dish.price * quantity)
    });
  },

  onMinus() {
    if (this.data.quantity <= 1) return;
    this.updateQuantity(this.data.quantity - 1);
  },

  onAdd() {
    if (this.data.quantity >= 99) return;
    this.updateQuantity(this.data.quantity + 1);
  },

  // 加入购物车
  onAddToCart() {
    const dish = this.data.dish;
    cartUtil.addToCart(
      { id: dish._id, name: dish.name, price: dish.price, image: dish.image },
      this.data.quantity
    );
    wx.showToast({ title: '已加入购物车', icon: 'success' });
    setTimeout(() => wx.navigateBack(), 600);
  }
});
