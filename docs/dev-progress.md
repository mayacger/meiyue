# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I10 已完成**（持续开发授权 C19；二期能力包：平台券/评价/搜索/装修增强/站内通知骨架）  
> 仓库：`mayacger/meiyue` · 分支：`cursor/meiyue-mall-scaffold-9727` · PR #1  
> 规划：`ecommerce-platform-plan.md` / `project-context.md` **v1.2 + C19**  
> I10 详档：`docs/i10-phase2-capabilities.md`

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

## I1–I9（摘要）

- I1 入驻鉴权 · I2 商品装修 · I3 购物车下单 · I4 支付安全 · I5 正向物流 · I6 售后48h · I7 可观测串租 · I8 AI 图/详情 · I9 Redis/店券/结算骨架  
- 详阅历史章节与模块 README

---

## I10 已交付（二期能力包）

### 平台券

- Flyway `V10`：`platform_coupons` / `platform_coupon_claims`
- Admin 发放 · 买家领取 · 下单 `platformCouponClaimId` 抵扣
- **与店券默认互斥** `meiyue.coupon.stacking=MUTUAL_EXCLUSIVE`（`CouponStackingRules` + 单测）

### 商品评价

- 买家 `confirm-receipt` → `COMPLETED` 后方可评
- 商品详情公开评价；商家列表与回复（trade 模块）

### 基础搜索

- `GET /products?q=&categoryId=`：标题/副标题/类目名 LIKE

### 装修增强

- 更多楼层类型 + `sortOrder` 规范化；仍模板+配置；禁止直播

### 站内通知骨架

- `notifications` 表 + `/notifications` / `/admin/notifications`

### I10 验证

```bash
cd backend && mvn -q -pl meiyue-trade -am test -Dtest=CouponStackingRulesTest
cd backend && mvn -q -DskipTests package
# 平台券
POST /api/v1/admin/platform-coupons
POST /api/v1/buyer/platform-coupons/{id}/claim
POST /api/v1/buyer/orders/checkout  {"platformCouponClaimId":1}
# 互斥应 400
POST .../checkout  {"storeCouponClaimId":1,"platformCouponClaimId":1}
# 搜索 / 评价 / 通知
GET /api/v1/products?q=关键词
POST /api/v1/buyer/orders/{id}/confirm-receipt
POST /api/v1/buyer/reviews
GET /api/v1/notifications
```

---

## 下一步

- 真实微信/支付宝与 AI 外呼联调；Redis 多实例分布式锁
- 官方分账打款 / AI 推广视频（明确不做于本迭代）
- 通知事件自动投递、装修可视化编辑（非自由拖拽）

---

## 已知限制

- 跨店订单暂不支持店券；平台券与店券本迭代不叠加
- Redis 宕机时依赖 DB 兜底与内存限流降级
- 内容安全 / 轨迹查询仍为占位
- 售后退款与结算仍为账本记账；站内通知无推送通道
