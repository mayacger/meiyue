# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I14 已完成**（C19；C20 前端四端硬约束）  
> 仓库：`mayacger/meiyue` · 分支：`cursor/meiyue-mall-scaffold-9727` · PR #1  
> 前端架构：Context `frontend-architecture.md`

---

## 入口

| 入口 | 说明 |
|------|------|
| API | `backend/meiyue-boot` → `:8080` |
| 买家 PC | `frontend/apps/web-buyer` → `:5173` |
| 商家 Pro | `frontend/apps/web-seller` → `:5174` |
| 平台 Pro | `frontend/apps/web-admin` → `:5175` |
| 买家 Taro | `frontend/apps/taro-buyer` → H5 `:10086` |
| CI | `.github/workflows/ci.yml` |

---

## I14 验证

```bash
cd frontend && pnpm install && pnpm build && pnpm build:taro:h5
```

已本地通过：web-admin / web-seller / web-buyer / taro `build:h5`。

---

## 下一步 I15（C19 自动）

Pro 业务页深度 + Taro 下单链路。
