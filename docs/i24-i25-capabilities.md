# I24 / I25 能力说明

> 入口：同一 PR · 无新增 Flyway（复用 `audit_logs` / `notifications` / 库存表）  
> **不做**：直播、真实分账、密钥联调

## 关系图

```
PUT /auth/profile · POST /auth/password
        └── UserAccount（displayName/phone/passwordHash）

GET /admin/audit-logs ──► audit_logs（只读筛选）

GET /buyer/orders/{id}/shipments.tracks
GET /seller/shipments（expand 时间线）
        └── TrackTimeline / Taro 轨迹列表

POST /seller/products/batch-status
GET /seller/inventory/alerts?threshold=5

GET /notifications · unread-count · read · read-all
        └── 消息中心全部/未读筛选
```

## I24 API / 页面

| 能力 | 端点 / 入口 |
|------|-------------|
| 资料 | `PUT /api/v1/auth/profile` · Buyer `/settings` · Seller `/settings` · Admin 账号页 |
| 改密 | `POST /api/v1/auth/password` |
| 审计 | `GET /api/v1/admin/audit-logs` · Admin `/audit-logs` |
| 轨迹 | Buyer 订单详情 · Taro 订单详情 · Seller 发货展开行 |

## I25

| 能力 | 端点 / 入口 |
|------|-------------|
| 消息已读未读 | 已有 API + 各端全部/未读筛选 |
| 批量上下架 | `POST /api/v1/seller/products/batch-status` |
| 库存预警 | `GET /api/v1/seller/inventory/alerts?threshold=5` · 库存页 Tab |

## 冒烟

`scripts/smoke-e2e.sh`、`scripts/smoke-i18.sh` 已覆盖 I24/I25 关键路径。
