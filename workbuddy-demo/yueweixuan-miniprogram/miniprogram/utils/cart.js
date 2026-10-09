// 购物车：本地存储实现，结构 [{ id, name, price, image, quantity }]
const CART_KEY = 'cart_list';

function getCart() {
  return wx.getStorageSync(CART_KEY) || [];
}

function saveCart(list) {
  wx.setStorageSync(CART_KEY, list);
}

// 加入购物车（dish 含 id/name/price/image）
function addToCart(dish, quantity = 1) {
  const list = getCart();
  const idx = list.findIndex((it) => it.id === dish.id);
  if (idx >= 0) {
    list[idx].quantity += quantity;
  } else {
    list.push({
      id: dish.id,
      name: dish.name,
      price: dish.price,
      image: dish.image,
      quantity
    });
  }
  saveCart(list);
  return list;
}

// 修改数量（quantity <= 0 时移除该项）
function updateQuantity(id, quantity) {
  let list = getCart();
  if (quantity <= 0) {
    list = list.filter((it) => it.id !== id);
  } else {
    const idx = list.findIndex((it) => it.id === id);
    if (idx >= 0) list[idx].quantity = quantity;
  }
  saveCart(list);
  return list;
}

function removeItem(id) {
  const list = getCart().filter((it) => it.id !== id);
  saveCart(list);
  return list;
}

function clearCart() {
  wx.removeStorageSync(CART_KEY);
}

// 合计
function getTotals() {
  const list = getCart();
  let count = 0;
  let amount = 0;
  list.forEach((it) => {
    count += it.quantity;
    amount += it.price * it.quantity;
  });
  return { count, amount };
}

// 购物车快照（含展示字段），供页面 setData
function getCartView() {
  const list = getCart();
  const { count, amount } = getTotals();
  return {
    items: list,
    totalCount: count,
    totalAmount: amount
  };
}

module.exports = {
  getCart,
  addToCart,
  updateQuantity,
  removeItem,
  clearCart,
  getTotals,
  getCartView
};
