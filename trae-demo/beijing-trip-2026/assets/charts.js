// assets/charts.js — 航班票价对比图
(function () {
  var style = getComputedStyle(document.documentElement);
  var accent = style.getPropertyValue('--accent').trim();
  var accent2 = style.getPropertyValue('--accent2').trim();
  var ink = style.getPropertyValue('--ink').trim();
  var muted = style.getPropertyValue('--muted').trim();
  var rule = style.getPropertyValue('--rule').trim();
  var bg2 = style.getPropertyValue('--bg2').trim();

  function makeBarChart(elId, rows) {
    // rows: [{ name, time, price, best }]
    var chart = echarts.init(document.getElementById(elId), null, { renderer: 'svg' });
    chart.setOption({
      animation: false,
      grid: { left: 8, right: 56, top: 10, bottom: 10, containLabel: true },
      tooltip: {
        trigger: 'item',
        appendToBody: true,
        backgroundColor: bg2,
        borderColor: rule,
        textStyle: { color: ink, fontSize: 13 },
        formatter: function (p) {
          var r = rows[p.dataIndex];
          return '<b>' + r.name + '</b><br/>' + r.time + '<br/>经济舱：<b style="color:' + accent + '">¥' + r.price + '</b>';
        }
      },
      xAxis: {
        type: 'value',
        axisLabel: { color: muted, fontSize: 11, formatter: '¥{value}' },
        splitLine: { lineStyle: { color: rule } }
      },
      yAxis: {
        type: 'category',
        inverse: true,
        data: rows.map(function (r) { return r.name; }),
        axisLabel: {
          color: ink, fontSize: 12,
          formatter: function (name, idx) {
            return name + (rows[idx].best ? '  ★' : '');
          }
        },
        axisLine: { lineStyle: { color: rule } },
        axisTick: { show: false }
      },
      series: [{
        type: 'bar',
        barWidth: 16,
        data: rows.map(function (r) {
          return {
            value: r.price,
            itemStyle: {
              color: r.best ? accent : accent2,
              borderRadius: [0, 4, 4, 0],
              opacity: r.best ? 1 : 0.75
            }
          };
        }),
        label: {
          show: true, position: 'right',
          color: ink, fontSize: 12, fontWeight: 600,
          formatter: function (p) { return '¥' + p.value; }
        }
      }]
    });
    window.addEventListener('resize', function () { chart.resize(); });
  }

  // 去程：11.15 广州 → 北京（★ 为主推）
  makeBarChart('chart-dep', [
    { name: 'KN5830 中联航', time: '19:55 白云 → 大兴', price: 670, best: false },
    { name: 'JD5922 首航', time: '20:25 白云 → 大兴', price: 1240, best: false },
    { name: 'HU7802 海航', time: '08:00 白云 → 首都', price: 2140, best: true },
    { name: 'MU6302 东航', time: '07:25 白云 → 大兴', price: 2620, best: false },
    { name: 'CA1380 国航', time: '09:30 白云 → 首都', price: 2780, best: false },
    { name: 'CZ3101 南航', time: '08:00 白云 → 大兴', price: 2940, best: false },
    { name: 'CA1310 国航', time: '08:00 白云 → 首都', price: 3360, best: false }
  ]);

  // 返程：11.17 北京 → 广州（★ 为主推）
  makeBarChart('chart-ret', [
    { name: 'CZ3178 南航', time: '20:30 大兴 → 白云', price: 550, best: false },
    { name: 'HU7815 海航', time: '20:30 首都 → 白云', price: 610, best: true },
    { name: 'CZ3116 南航', time: '07:30 大兴 → 白云', price: 620, best: false },
    { name: 'CA1379 国航', time: '19:15 首都 → 白云', price: 660, best: false },
    { name: 'CA1301 国航', time: '15:00 首都 → 白云', price: 800, best: false },
    { name: 'CA1339 国航', time: '11:55 首都 → 白云', price: 800, best: false },
    { name: 'HU7801 海航', time: '08:30 首都 → 白云', price: 850, best: false }
  ]);
})();
