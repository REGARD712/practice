'use strict';
/**
 * 知识问答 Agent —— 纯函数单元测试
 * 直接从 index.html 的 <script> 中提取真实函数源码进行测试，避免与实现漂移。
 * 运行: node test/unit-tests.js
 */
const fs = require('fs');
const path = require('path');

const html = fs.readFileSync(path.join(__dirname, '..', 'index.html'), 'utf8');
const scriptMatch = html.match(/<script>\n([\s\S]*?)<\/script>/);
if (!scriptMatch) { console.error('FATAL: 未找到 <script> 块'); process.exit(1); }
const src = scriptMatch[1];

/** 按花括号配对提取顶层函数定义（兼容 async 前缀） */
function extractFn(name) {
  let start = src.indexOf('async function ' + name + '(');
  if (start < 0) start = src.indexOf('function ' + name + '(');
  if (start < 0) throw new Error('未找到函数: ' + name);
  let i = src.indexOf('{', start);
  let depth = 0;
  for (let j = i; j < src.length; j++) {
    if (src[j] === '{') depth++;
    else if (src[j] === '}') { depth--; if (depth === 0) return src.slice(start, j + 1); }
  }
  throw new Error('函数未闭合: ' + name);
}

/** 把函数声明编译为可调用的函数对象，可注入依赖 */
function loadFn(name, deps) {
  const code = extractFn(name);
  const depNames = Object.keys(deps || {});
  const factory = new Function(...depNames, 'return (' + code + ');');
  return factory(...depNames.map((k) => deps[k]));
}

/* ---------- 加载被测函数（注入最小依赖） ---------- */
const escapeHtml = loadFn('escapeHtml');
const renderSafe = loadFn('renderSafe', { escapeHtml });
const truncate = loadFn('truncate');
const formatTime = loadFn('formatTime');
const describeError = loadFn('describeError');
const extractKeywords = loadFn('extractKeywords');
// buildMessages 依赖全局 state，注入可控 state
const state = { history: [], HISTORY_MAX_TURNS: 4 };
const buildMessages = loadFn('buildMessages', { state });
// retrieveKnowledge 依赖 cloud + extractKeywords，注入 stub cloud 捕获 or() 参数
let capturedOr = null;
const stubCloud = {
  database: {
    from() {
      const q = {
        select() { return q; }, order() { return q; }, limit() { return q; },
        or(cond) { capturedOr = cond; return q; },
        then(resolve) { resolve({ data: [], error: null }); },
      };
      return q;
    },
  },
};
const retrieveKnowledge = loadFn('retrieveKnowledge', { cloud: stubCloud, extractKeywords });

/* ---------- 极简断言框架 ---------- */
let pass = 0, fail = 0;
const failures = [];
function eq(actual, expected, name) {
  const a = JSON.stringify(actual), e = JSON.stringify(expected);
  if (a === e) { pass++; }
  else { fail++; failures.push({ name, expected: e, actual: a }); }
}
function ok(cond, name, extra) {
  if (cond) { pass++; }
  else { fail++; failures.push({ name, expected: 'truthy', actual: String(cond) + (extra ? ' | ' + extra : '') }); }
}

/* ================= escapeHtml ================= */
eq(escapeHtml('<script>alert(1)</script>'),
  '&lt;script&gt;alert(1)&lt;/script&gt;', 'escapeHtml: script 标签转义');
eq(escapeHtml(`&<>"'`), '&amp;&lt;&gt;&quot;&#39;', 'escapeHtml: 五种特殊字符全转义');
eq(escapeHtml(null), '', 'escapeHtml: null -> 空串');
eq(escapeHtml(undefined), '', 'escapeHtml: undefined -> 空串');
eq(escapeHtml(123), '123', 'escapeHtml: 数字输入转字符串');
ok(!escapeHtml('<img src=x onerror=alert(1)>').includes('<img'),
  'escapeHtml: onerror 注入被转义');

/* ================= renderSafe ================= */
eq(renderSafe('a\nb'), 'a<br>b', 'renderSafe: 换行转 <br>');
eq(renderSafe('<b>x</b>\ny'), '&lt;b&gt;x&lt;/b&gt;<br>y', 'renderSafe: 先转义再转换行');
ok(!renderSafe('<script>\n</script>').includes('<script>'), 'renderSafe: 模型输出 XSS 被拦截');

/* ================= truncate ================= */
eq(truncate('abcdef', 3), 'abc…', 'truncate: 超长截断加省略号');
eq(truncate('abc', 3), 'abc', 'truncate: 边界等长不截断');
eq(truncate(null, 5), '', 'truncate: null 安全');

/* ================= formatTime ================= */
eq(formatTime(''), '', 'formatTime: 空值');
eq(formatTime('not-a-date'), 'not-a-date', 'formatTime: 非法日期原样返回');
ok(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/.test(formatTime('2024-05-01T08:09:10Z')),
  'formatTime: 合法 ISO 输出 YYYY-MM-DD HH:mm', formatTime('2024-05-01T08:09:10Z'));

/* ================= describeError ================= */
eq(describeError({ error: { code: 'quota_exceeded' } }),
  '模型调用额度不足，请稍后再试或联系管理员充值。', 'describeError: quota_ 前缀');
eq(describeError({ error: { code: 'gateway_timeout' } }),
  '模型服务暂时不可用，请稍后重试。', 'describeError: gateway_ 前缀');
eq(describeError({ error: { code: 'model_error' } }),
  '模型服务暂时不可用，请稍后重试。', 'describeError: model_ 前缀');
eq(describeError({ error: { code: 'auth_invalid' } }),
  '服务凭证异常，请检查应用配置或联系管理员。', 'describeError: auth_ 前缀');
eq(describeError({ name: 'AbortError' }), '已停止生成。', 'describeError: AbortError');
eq(describeError(new Error('network down')), '请求失败：network down', 'describeError: 普通 Error');
ok(describeError(null).startsWith('请求失败：'), 'describeError: null 不崩溃');

/* ================= extractKeywords ================= */
eq(extractKeywords(''), [], 'extractKeywords: 空输入');
eq(extractKeywords('   '), [], 'extractKeywords: 纯空白');
eq(extractKeywords('什么 怎么 如何 为什么 请问'), [], 'extractKeywords: 纯停用词（空格分隔）');
// 行为记录：无空格连写的中文不做分词，整串会成为关键词（依赖 ilike 子串匹配兜底）
eq(extractKeywords('的了吗是吗呢吧'), ['的了吗是吗呢吧'], 'extractKeywords: 无空格连写中文按整词处理（记录行为）');
ok(extractKeywords('什么是 HashMap 的原理？').includes('HashMap'),
  'extractKeywords: 提取英文技术词', JSON.stringify(extractKeywords('什么是 HashMap 的原理？')));
ok(extractKeywords('HashMap 和 Hashtable 的区别？').length <= 3,
  'extractKeywords: 最多 3 个关键词');
eq(extractKeywords('hashmap HashMap HASHMAP').length, 3,
  'extractKeywords: 大小写视为不同 token（记录实际行为）');
eq(extractKeywords('😀😀 😃😃').length > 0, true, 'extractKeywords: emoji 输入不崩溃');
{
  const kws = extractKeywords('100% 命中率,保证');
  ok(kws.every((k) => !k.includes(',')), 'extractKeywords: 逗号被标点规则去除');
}
{
  const long = '超长'.repeat(500) + ' HashMap ' + '噪音'.repeat(500);
  const kws = extractKeywords(long);
  ok(kws.length <= 3, 'extractKeywords: 超长输入仍有界');
}

/* ================= retrieveKnowledge（or 条件装配与通配符防护） ================= */
(async () => {
  capturedOr = null;
  await retrieveKnowledge('100%  guaranteed, yes');
  ok(capturedOr !== null, 'retrieveKnowledge: 有关键词时调用 or()');
  const valueParts = capturedOr.split(',').map((c) => c.replace(/^title\.ilike\.%|%$|^content\.ilike\.%|%$/g, ''));
  // 统计 % 出现次数：每个条件应恰好 2 个通配符（前后各一），用户输入的 % 必须被剥除
  const pctCount = (capturedOr.match(/%/g) || []).length;
  const kwCount = extractKeywords('100%  guaranteed, yes').length;
  eq(pctCount, kwCount * 4, 'retrieveKnowledge: 用户输入的 % 通配符被剥除', capturedOr);
  ok(!/^(title|content)\.ilike\.[^%]/.test(capturedOr), 'retrieveKnowledge: 条件格式 title.ilike.%kw% 正确');

  capturedOr = null;
  await retrieveKnowledge('什么 怎么 如何'); // 全部为停用词 -> 无有效关键词
  eq(capturedOr, null, 'retrieveKnowledge: 无关键词时不拼 or()（走兜底最近条目）');

  /* ================= buildMessages ================= */
  state.history = [];
  const entries = [
    { category: 'Java 基础', title: 'HashMap 原理', content: '数组+链表+红黑树' },
    { category: null, title: '无分类条目', content: '内容B' },
  ];
  const msgs = buildMessages('HashMap 怎么扩容？', entries);
  eq(msgs[0].role, 'system', 'buildMessages: messages[0] 必须是 system');
  ok(msgs[0].content.includes('HashMap 原理') && msgs[0].content.includes('数组+链表+红黑树'),
    'buildMessages: 知识库条目注入 system prompt');
  ok(msgs[0].content.includes('(未分类)'), 'buildMessages: 空分类兜底为 未分类');
  eq(msgs[msgs.length - 1], { role: 'user', content: 'HashMap 怎么扩容？' },
    'buildMessages: 末条为当前用户问题');

  const msgsEmpty = buildMessages('没有知识的问题', []);
  ok(msgsEmpty[0].content.includes('未检索到相关知识库条目'),
    'buildMessages: 空知识库时 system 明确说明');

  state.history = [];
  for (let i = 0; i < 10; i++) {
    state.history.push({ role: 'user', content: 'q' + i });
    state.history.push({ role: 'assistant', content: 'a' + i });
  }
  const msgsBounded = buildMessages('current', []);
  // 1 system + 4 轮*2 + 1 当前问题 = 10
  eq(msgsBounded.length, 10, 'buildMessages: 历史注入有界（最多 4 轮）');
  eq(msgsBounded[1].content, 'q6', 'buildMessages: 保留的是最近的历史');
  ok(msgsBounded.slice(1, -1).every((m) => m.role === 'user' || m.role === 'assistant'),
    'buildMessages: 历史仅含 user/assistant 角色');

  /* ================= 分页计算（复刻 renderKbPager / range 逻辑验证） ================= */
  function rangeOf(page, pageSize) { return [(page - 1) * pageSize, page * pageSize - 1]; }
  function totalPages(total, pageSize) { return Math.max(1, Math.ceil(total / pageSize)); }
  eq(rangeOf(1, 5), [0, 4], '分页: 第1页 range [0,4]');
  eq(rangeOf(3, 5), [10, 14], '分页: 第3页 range [10,14]');
  eq(totalPages(0, 5), 1, '分页: 0 条时总页数为 1');
  eq(totalPages(5, 5), 1, '分页: 恰好整页');
  eq(totalPages(11, 5), 3, '分页: 11 条 3 页');

  /* ================= 报告 ================= */
  console.log('========================================');
  console.log('总计: ' + (pass + fail) + ' | 通过: ' + pass + ' | 失败: ' + fail);
  if (failures.length) {
    console.log('\n失败明细:');
    failures.forEach((f) => console.log('  ✗ ' + f.name + '\n      期望: ' + f.expected + '\n      实际: ' + f.actual));
    process.exit(1);
  }
  console.log('ALL_TESTS_PASSED');
})().catch((e) => { console.error('测试运行异常:', e); process.exit(1); });
