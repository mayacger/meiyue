# 美月商城 · 平台后台（web-admin）

## 入口

- 包名：`@meiyue/web-admin`
- 开发：`pnpm --filter @meiyue/web-admin dev` → http://localhost:5175
- 技术：React + Vite + **Ant Design Pro Components**（ProLayout / ProTable / ProForm）

## 关系图

```text
LoginPage ──JWT──► AdminLayout(ProLayout)
                      ├── OnboardingPage  → /api/v1/admin/onboarding/*
                      ├── CouponsPage     → /api/v1/admin/platform-coupons
                      └── NotificationsPage → /api/v1/admin/notifications
```

## 种子账号

`admin` / `admin123`
