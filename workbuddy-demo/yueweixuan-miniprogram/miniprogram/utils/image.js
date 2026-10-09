// 图片地址解析
// - "/..." 开头：小程序内置静态资源（菜品图随包分发），直接返回
// - "http..." 开头：已是完整链接，直接返回
// - 其他：云存储对象路径（如用户头像），通过 createSignedUrl 换取临时访问链接
const { cloud } = require('./cloud');

const PLACEHOLDER = '/assets/images/dish-1.png';
const signedCache = {}; // 签名链接有效期最长 1 小时，页面生命周期内复用即可

// createSignedUrl 返回 { data, error }；防御性兼容字符串 / 对象两种形态
function extractUrl(res) {
  if (!res) return '';
  if (typeof res === 'string') return res;
  const d = res.data !== undefined ? res.data : res;
  if (typeof d === 'string') return d;
  if (d && typeof d === 'object') return d.url || d.signedUrl || d.signed_url || '';
  return '';
}

async function resolveImage(src) {
  if (!src) return PLACEHOLDER;
  if (src.indexOf('/') === 0) return src;
  if (src.indexOf('http') === 0) return src;

  if (signedCache[src]) return signedCache[src];
  try {
    const url = extractUrl(await cloud.storage.createSignedUrl(src, 3600));
    if (url) {
      signedCache[src] = url;
      return url;
    }
    return '';
  } catch (err) {
    // 签名失败（网络异常 / 权限不足）：返回空串，由页面按无头像处理
    return '';
  }
}

// 批量解析：为列表中每项生成 imageSrc 字段
async function resolveList(list, key = 'image') {
  const tasks = (list || []).map(async (it) => {
    const src = await resolveImage(it[key]);
    return { ...it, imageSrc: src };
  });
  return Promise.all(tasks);
}

module.exports = { resolveImage, resolveList };
