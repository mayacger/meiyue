# 美月商城前端（pnpm monorepo）

## 入口

| 应用 | 包名 | 本地端口 | 规划路径 |
|------|------|----------|----------|
| 买家商城 | `@meiyue/web-buyer` | 5173 | `/` |
| 商家后台 | `@meiyue/web-seller` | 5174 | `/seller` |
| 平台后台 | `@meiyue/web-admin` | 5175 | `/admin` |

共享包：`@meiyue/ui`（页面壳）、`@meiyue/types`（API 类型占位）。

## 关系图

```text
frontend/
  apps/web-buyer  ──depends──► packages/ui + packages/types
  apps/web-seller ──depends──► packages/ui + packages/types
  apps/web-admin  ──depends──► packages/ui + packages/types
         │
         └── vite proxy /api → http://localhost:8080 (meiyue-boot)
```

## 本地启动

```bash
# 需要 Node >= 20、pnpm >= 9
cd frontend
pnpm install
pnpm --filter @meiyue/web-buyer build   # 验证构建
pnpm dev:buyer                          # 开发模式
```

## 说明文档约定

每页/功能后续在对应 `apps/*/src/pages` 旁或 `packages/domain-docs` 补充：入口、关系图、状态、验收标准。
