# 美月商城 · 平台后台（web-admin）

## 入口

- 包名：`@meiyue/web-admin`
- 开发：`pnpm --filter @meiyue/web-admin dev` → http://localhost:5175
- 技术：React + Vite + Ant Design Pro Components

## 关系图

```text
Login → AdminLayout(ProLayout + 角色 Tag)
  ├── Dashboard / Onboarding / Coupons
  ├── Notifications / Categories(只读) / Account
  └── JWT → /api/v1/admin/* · /categories · /auth/me
```

## 种子

`admin` / `admin123`
