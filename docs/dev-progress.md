# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I9 已完成**（持续开发授权 C19；含 Redis 加固、前端补齐、结算骨架、店券最小闭环）  
> 仓库：`mayacger/meiyue` · 分支：`cursor/meiyue-mall-scaffold-9727` · PR #1  
> 规划：`ecommerce-platform-plan.md` / `project-context.md` **v1.2 + C19**

---

## 入口

| 入口 | 说明 |
|------|------|
| API | `backend/meiyue-boot` → `:8080` |
| 商家端 | `frontend/apps/web-seller` → `:5174` |
| 平台端 | `frontend/apps/web-admin` → `:5175` |
| 买家端 | `frontend/apps/web-buyer` → `:5173` |
| Compose | `docker/docker-compose.yml`（PostgreSQL + Redis） |

种子：`admin`/`admin123`；联调 `seller1`/`seller123`、`buyer1`/`buyer123`

---

## I1–I8（摘要）

- I1 入驻鉴权 · I2 商品装修 · I3 购物车下单 · I4 支付安全 · I5 正向物流 · I6 售后48h · I7 可观测串租 · I8 AI 图/详情  
- 详阅历史章节与模块 README

---

## I9 已交付（本轮加固 + 二期启动）

### Redis 接入

- `docker-compose` 默认启动 Redis；应用启用 `RedisAutoConfiguration`
- 延迟队列 ZSET：关单 `ORDER_EXPIRE`、售后自动同意 `AFTERSALE_AUTO`
- 限流优先 Redis INCR，失败降级内存
- 可选 AI 任务 List 队列 + 消费 Job
- **DB 扫描兜底**：`meiyue.jobs.db-fallback-enabled=true`（默认开）

### 前端补齐

- Seller：`/shipments` 发货与状态推进；`/aftersales` 审核；`/settlements` 账本；`/coupons` 发券
- Buyer：`/orders/:id` 物流轨迹 + 申请售后 + 填逆向运单
- Admin：入驻审核已有，本轮未另做审计页

### 结算增强骨架

- `GET /api/v1/seller/settlements/bills`、`/periods`（周期汇总）
- 仍为 MVP 记账，**不接官方分账打款**

### 店券（二期最小）

- Flyway `V9__i9_store_coupons.sql`
- 商家发券 / 买家领券 / 下单 `couponClaimId` 抵扣（单店订单）
- 平台券后置；不做直播 / 不做 AI 视频

### I9 验证

```bash
redis-cli ping
cd backend && mvn -q -DskipTests package
# 结算
GET /api/v1/seller/settlements/periods
# 店券
POST /api/v1/seller/coupons
POST /api/v1/buyer/coupons/{id}/claim
POST /api/v1/buyer/orders/checkout  {"couponClaimId":1}
# Redis 键
redis-cli keys 'meiyue:*'
```

---

## 下一步

- 真实微信/支付宝与 AI 外呼联调；Redis 多实例分布式锁
- 平台券 / 官方分账打款 / AI 推广视频（二期）
- 前端体验与装修拖拽增强

---

## 已知限制

- 跨店订单暂不支持店券
- Redis 宕机时依赖 DB 兜底与内存限流降级
- 内容安全 / 轨迹查询仍为占位
- 售后退款与结算仍为账本记账
