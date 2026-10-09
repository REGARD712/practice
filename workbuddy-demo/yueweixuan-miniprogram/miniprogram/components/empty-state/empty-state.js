Component({
  properties: {
    // 空状态图（统一风格图标，默认为餐盘空状态图）
    icon: { type: String, value: '/assets/images/empty.png' },
    text: { type: String, value: '暂无数据' },
    subText: { type: String, value: '' },
    buttonText: { type: String, value: '' }
  },
  methods: {
    onAction() {
      this.triggerEvent('action');
    }
  }
});
