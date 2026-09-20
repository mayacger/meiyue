# 美月商城 · 脚手架 / 迭代进度说明（用户可读）

> 状态：**I1 → I6 已完成**（用户授权持续开发，见 project-context **C19**）  
> 详细验证步骤见同目录 `dev-progress.md`  
> 仓库：`mayacger/meiyue` · PR #1

---

## 1. 入口一览

| 入口 | 路径 / 命令 | 说明 |
|------|-------------|------|
| 根说明 | 仓库 `README.md` | 启动方式 |
| 进度（详） | `docs/dev-progress.md` | I1–I6 验证 |
| API | `meiyue-boot` :8080 | JWT + 业务 API |
| 商家端 | `web-seller` :5174 | 入驻 / 商品 / 装修 |
| 平台端 | `web-admin` :5175 | 入驻审核 |
| 买家端 | `web-buyer` :5173 | 浏览商品与店铺页 |

---

## 2. 已完成迭代

### I1 — 登录、入驻、开店

账号 JWT、RBAC、Flyway V1、入驻审核开店闭环、seller/admin 最小页。

### I2 — 商品与模板装修

类目、SPU/SKU、上下架、买家浏览；装修模板+草稿发布（无直播）；Flyway V2。

### I3 — 购物车 / 下单 / 模拟支付

跨店购物车、预占库存、MOCK 支付、30min 超时关单；Flyway V3。

### I4 — 支付安全基线

微信/支付宝骨架、验签/幂等/回调/查单/日对账占位、结算账本 MVP、密钥走环境变量；Flyway V4。

### I5 — 正向物流

全状态机 + 轨迹占位查询 + 订单 FULFILLING/COMPLETED；Flyway V5。

### I6 — 售后与逆向

仅退款/退货退款、逆向运单、48h 自动同意、账本 REFUND；Flyway V6。

---

## 3. 下一步

**I7+** Redis / 通知 / 前端发货售后页 / 真实通道证书联调；官方分账二期。

---

## 4. 构建自检

```bash
cd backend && mvn -q -DskipTests package
cd frontend && pnpm install && pnpm -r --filter "./apps/*" build
```
