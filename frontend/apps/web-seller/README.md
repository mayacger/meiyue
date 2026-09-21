# 美月商城 · 商家后台（web-seller）

## 入口

- 包名：`@meiyue/web-seller`
- 开发：`pnpm --filter @meiyue/web-seller dev` → http://localhost:5174
- 技术：React + Vite + Ant Design Pro Components

## 关系图

```text
Login/Register → Onboarding → SellerLayout(ProLayout)
  ├── Dashboard / Products / Decoration
  ├── Shipments / Aftersales / Settlements
  ├── Coupons / AI
  └── JWT → /api/v1/seller/*
```

## 演示账号

`seller1` / `seller123`（demo profile）
