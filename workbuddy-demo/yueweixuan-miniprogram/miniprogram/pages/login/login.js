// 登录页：微信一键登录 + 完善头像昵称
// 两段式：未登录 → 微信一键登录（wx.login 换取 WorkBuddy 会话）；
//         已登录 → 头像昵称表单（官方头像昵称填写能力，头像存云存储）
// 说明：wx.getUserProfile 已被平台收回，官方推荐 open-type="chooseAvatar"
// 与 input type="nickname" 的头像昵称填写能力
const app = getApp();
const api = require('../../utils/api');
const { resolveImage } = require('../../utils/image');

Page({
  data: {
    stage: 'loading',   // loading 检测登录态 | signin 微信一键登录 | profile 完善资料
    signing: false,     // 微信登录请求中
    avatarTemp: '',     // 新选头像的本地临时路径（上传前）
    avatarCurrent: '',  // 已保存头像的签名链接（回显）
    nickname: '',
    submitting: false
  },

  onLoad() {
    this.init();
  },

  // 检测登录态：已有会话直接进入资料步骤，否则展示微信一键登录
  async init() {
    const session = await api.getSession();
    if (session && session.user && session.user.id) {
      app.globalData.session = session;
      await this.enterProfileStage();
    } else {
      this.setData({ stage: 'signin' });
    }
  },

  // 进入资料步骤：读取云端资料，昵称回填、头像换签名链接回显
  async enterProfileStage() {
    const profile = await api.getProfile();
    let avatarCurrent = '';
    if (profile && profile.avatar) {
      avatarCurrent = await resolveImage(profile.avatar);
    }
    this.setData({
      stage: 'profile',
      nickname: (profile && profile.nickname) || '',
      avatarCurrent
    });
  },

  // 步骤一：微信一键登录（appid 取运行时值，试用版与正式版是不同小程序）
  async onWechatLogin() {
    if (this.data.signing) return;
    this.setData({ signing: true });
    try {
      await api.signInWechat();
      await app.refreshUserState();
      await this.enterProfileStage();
    } catch (err) {
      // 含登录开关未开启 / 小程序未绑定等平台侧原因，展示后端返回信息
      const message = (err && err.message) || '微信登录失败，请重试';
      wx.showToast({ title: message, icon: 'none' });
    } finally {
      this.setData({ signing: false });
    }
  },

  // 选择微信头像（官方头像昵称填写能力，返回本地临时文件路径）
  onChooseAvatar(e) {
    const { avatarUrl } = e.detail || {};
    if (avatarUrl) this.setData({ avatarTemp: avatarUrl });
  },

  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value });
  },

  // 步骤二：上传头像到云存储 → 保存资料到 user_profiles
  async onSubmit() {
    const { avatarTemp, avatarCurrent, nickname, submitting } = this.data;
    if (submitting) return;

    if (!avatarTemp && !avatarCurrent) {
      wx.showToast({ title: '请先选择头像', icon: 'none' });
      return;
    }
    const name = (nickname || '').trim();
    if (!name) {
      wx.showToast({ title: '请填写昵称', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    try {
      let avatarPath = '';
      if (avatarTemp) {
        // 头像上传云存储（所有者路径 users/<uid>/avatars/...，需登录态）
        const session = app.globalData.session || await api.getSession();
        const userId = session && session.user && session.user.id;
        if (!userId) {
          wx.showToast({ title: '登录状态已失效，请重新登录', icon: 'none' });
          this.setData({ submitting: false });
          return;
        }
        avatarPath = await api.uploadAvatar(userId, avatarTemp);
      } else {
        // 未换头像：沿用云端已保存的存储路径
        const profile = await api.getProfile();
        avatarPath = (profile && profile.avatar) || '';
      }

      const saved = await api.saveProfile({ nickname: name, avatar: avatarPath });
      // 回填全局用户信息（含头像签名链接与首字），返回首页即可见
      await app.decorateUserInfo({ nickname: saved.nickname, avatar: saved.avatar });

      wx.showToast({ title: '登录成功', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 600);
    } catch (err) {
      console.error('[login] save profile failed:', err && err.message);
      const message = err && err.message === 'SAVE_BLOCKED'
        ? '登录状态已失效，请重新登录'
        : '保存失败，请重试';
      wx.showToast({ title: message, icon: 'none' });
      this.setData({ submitting: false });
    }
  }
});
