# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I15 进行中**（I14 架构已完成；C19 自动续 Pro 深度 + Taro 下单）  
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

- [x] Taro：购物车结算下单 + 我的订单模拟支付  
- [x] Pro：评价回复、发货状态推进/轨迹同步  
- [ ] 更多 Pro 深度页与 Taro 券互斥结算（持续）
