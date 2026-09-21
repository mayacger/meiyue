/**
 * 全局页面配置（I22/I23）
 * Tab：首页 / 分类 / 购物车 / 我的
 * 二级：详情 / 订单 / 地址 / 通知 / 收藏 / 足迹 / 店铺 / 领券
 */
export default defineAppConfig({
  pages: [
    "pages/index/index",
    "pages/category/index",
    "pages/cart/index",
    "pages/mine/index",
    "pages/detail/index",
    "pages/orders/index",
    "pages/order-detail/index",
    "pages/address/index",
    "pages/notifications/index",
    "pages/favorites/index",
    "pages/browse-history/index",
    "pages/store/index",
    "pages/coupons/index",
    "pages/settings/index"
  ],
  window: {
    backgroundTextStyle: "light",
    navigationBarBackgroundColor: "#0F3D38",
    navigationBarTitleText: "美月商城",
    navigationBarTextStyle: "white",
    backgroundColor: "#F7F6F3"
  },
  tabBar: {
    color: "#5A6B66",
    selectedColor: "#1A4D45",
    backgroundColor: "#F7F6F3",
    borderStyle: "black",
    list: [
      { pagePath: "pages/index/index", text: "首页" },
      { pagePath: "pages/category/index", text: "分类" },
      { pagePath: "pages/cart/index", text: "购物车" },
      { pagePath: "pages/mine/index", text: "我的" }
    ]
  }
});
