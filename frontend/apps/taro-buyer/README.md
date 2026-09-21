# 美月商城 · 买家移动端（taro-buyer）

## 入口

- 包名：`@meiyue/taro-buyer`
- 技术：**Taro 4 + React**，目标 **H5 + 微信小程序**（不用其它跨端框架）
- H5 开发：`pnpm --filter @meiyue/taro-buyer dev:h5` → http://localhost:10086
- H5 构建：`pnpm --filter @meiyue/taro-buyer build:h5`
- 小程序构建：`pnpm --filter @meiyue/taro-buyer build:weapp`（需微信开发者工具打开 `dist/weapp`）

## 关系图

```text
app.tsx（注入 Taro Storage）
  ├── pages/index     首页商品列表
  ├── pages/category  类目
  ├── pages/detail    详情 + 加购
  ├── pages/cart      购物车骨架
  └── pages/mine      登录 / 我的
        └── services/api.ts → Taro.request → /api/v1/*
```

## 本轮能力

- H5：登录（我的页）+ 商品列表可跑通（需后端 :8080）
- 下单链路：I15 补齐

## 说明

若 `build:h5` 因 Taro/Vite 版本组合失败，见仓库 `frontend/README.md` 排障；CI 主路径仍保证 admin/seller/buyer 三端 build。
