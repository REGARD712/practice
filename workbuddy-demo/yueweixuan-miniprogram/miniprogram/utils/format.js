// 金额与时间格式化
// 金额统一以「分」存储，展示时转为元，避免浮点精度问题
function fenToYuan(fen) {
  if (typeof fen !== 'number' || isNaN(fen)) return '0.00';
  return (fen / 100).toFixed(2);
}

// 时间戳/日期对象 → YYYY-MM-DD HH:mm
function formatTime(input) {
  const d = input instanceof Date ? input : new Date(input);
  if (isNaN(d.getTime())) return '';
  const pad = (n) => String(n).padStart(2, '0');
  return (
    d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) +
    ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
  );
}

module.exports = { fenToYuan, formatTime };
