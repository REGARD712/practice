// 只读探针：验证 PostgREST or 过滤器对特殊字符的容忍度（不修改任何数据）
const BASE = 'https://kb-qa-agent-54833.app.workbuddy.host/database/rest/knowledge_entries';
const KEY = 'wbpk_930QR98RTJXSXZ1Wpy7eGg_048vjV0HItzro6oWYrqMAXf0Z8Bi66Tx';
async function probe(label, orValue) {
  const url = BASE + '?select=id,title&limit=3&or=(' + encodeURIComponent(orValue) + ')';
  try {
    const r = await fetch(url, { headers: { apikey: KEY, Authorization: 'Bearer ' + KEY } });
    const body = await r.text();
    console.log(label, '-> HTTP', r.status, body.slice(0, 200));
  } catch (e) {
    console.log(label, '-> NETWORK_ERR', e.message);
  }
}
(async () => {
  await probe('基线(普通词)', 'title.ilike.%HashMap%,content.ilike.%HashMap%');
  await probe('带括号', 'title.ilike.%JVM(G1)%,content.ilike.%JVM(G1)%');
  await probe('带引号', 'title.ilike.%所谓"双亲"%,content.ilike.%所谓"双亲"%');
  await probe('带点号', 'title.ilike.%Node.js%,content.ilike.%Node.js%');
})();
