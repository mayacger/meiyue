# 美月商城 · 买家移动端（taro-buyer）

## 入口

- 包名：`@meiyue/taro-buyer`
- 技术：**Taro 4 + React**，目标 **H5 + 微信小程序**
- H5：`pnpm --filter @meiyue/taro-buyer dev:h5` → http://localhost:10086
- H5 构建：`pnpm --filter @meiyue/taro-buyer build:h5`
- 小程序：`pnpm --filter @meiyue/taro-buyer build:weapp` → 用微信开发者工具打开 `dist/weapp`（`project.config.json`）

## 关系图

```text
app.tsx
  ├── index / category / detail / cart
  ├── mine（登录 · 订单入口 · 地址）
  ├── orders / order-detail（支付 · 收货 · 售后）
  └── address（本地草稿占位）
```

## 能力（I15–I37）

- H5 登录、商品列表、加购、结算下单、模拟支付
- 订单详情确认收货、售后申请；地址/收藏/足迹
- 图集与推广视频（I34）；登录验证码可选（I35）
- 体积：见仓内 `docs/taro-bundle.md`（`optimizeMainPackage`）

无直播；不做其它跨端框架。
