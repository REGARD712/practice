// 应用入口：恢复云服务登录态与用户资料
const api = require('./utils/api');
const { resolveImage } = require('./utils/image');

App({
  globalData: {
    session: null,         // WorkBuddy Cloud 会话（含 user.id；未登录为 null）
    userInfo: null,        // 用户资料 { nickname, avatar(存储路径), avatarUrl(签名链接), firstChar }
    menuCache: null,       // 菜单缓存（categories + dishes，来自云端数据库）
    pendingCategoryId: ''  // 跨页传参：首页分类入口 → 点单页（switchTab 不能带参）
  },

  onLaunch() {
    // 启动即恢复登录态；页面通过 userStateReady 等待结果后再读 userInfo
    this.userStateReady = this.loadUserState();
  },

  // 恢复登录态：有效会话 → 读取云端资料 → 头像换签名链接；异常一律按未登录处理
  async loadUserState() {
    try {
      const session = await api.getSession();
      if (!session || !session.user || !session.user.id) {
        this.globalData.session = null;
        this.clearUserInfo();
        return null;
      }
      this.globalData.session = session;
      const profile = await api.getProfile();
      if (profile && profile.nickname) {
        await this.decorateUserInfo({ nickname: profile.nickname, avatar: profile.avatar || '' });
      } else {
        this.clearUserInfo();
      }
      return session;
    } catch (err) {
      // 会话恢复失败（网络异常等）按未登录处理，不打断启动
      this.globalData.session = null;
      return null;
    }
  },

  // 清空本地用户信息（未登录 / 无资料）
  clearUserInfo() {
    this.globalData.userInfo = null;
    wx.removeStorageSync('userInfo');
  },

  // 写入全局用户信息，并补齐展示字段（头像签名链接、昵称首字）
  async decorateUserInfo(info) {
    const base = {
      nickname: info.nickname || '',
      avatar: info.avatar || '',
      avatarUrl: '',
      firstChar: (info.nickname || '客').trim().charAt(0) || '客'
    };
    if (base.avatar) {
      base.avatarUrl = await resolveImage(base.avatar);
    }
    this.globalData.userInfo = base;
    wx.setStorageSync('userInfo', base);
    return base;
  },

  // 登录成功后由登录页调用：刷新全局会话（资料由登录页自行 decorate 后回填）
  async refreshUserState() {
    const session = await api.getSession();
    this.globalData.session = session || null;
    return session;
  }
});
