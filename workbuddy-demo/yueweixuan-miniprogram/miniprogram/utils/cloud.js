// 云服务客户端初始化（WorkBuddy Cloud · 小程序专用形态）
// endpoint 与 publishableKey 来自 workbuddy_cloud_service 返回的 publicConfig，
// 已固化在 workbuddy-cloud-config.js；小程序没有 location.origin，两者缺一不可。
const { createMiniProgramWorkBuddyCloud } = require('@tencent-ai/workbuddy-cloud-sdk/miniprogram');
const { createDiagnosticWx } = require('./workbuddy-cloud-diagnostics');
const publicConfig = require('./workbuddy-cloud-config');

const cloud = createMiniProgramWorkBuddyCloud({
  endpoint: publicConfig.endpoint,
  publishableKey: publicConfig.publishableKey,
  wx: createDiagnosticWx(wx)
});

module.exports = { cloud };
