# I10 二期能力包 · 说明文档

> 入口：`backend/meiyue-boot` · 前端 buyer/seller/admin · Flyway `V10__i10_platform_coupon_review_notify.sql`

## 关系图

```
平台 Admin ──发券──► platform_coupons
买家 ──领取──► platform_coupon_claims ──下单抵扣──► OrderService
卖家 ──发店券──► store_coupons / coupon_claims ───┘
                      │
                      ▼
              CouponStackingRules
              （默认 MUTUAL_EXCLUSIVE）

买家确认收货 ──► Order COMPLETED ──► product_reviews
                                      ├─ 商品详情公开 GET /products/{id}/reviews
                                      └─ 商家列表/回复 /seller/reviews

GET /products?q=&categoryId= ──► ProductRepository.searchOnSale（标题/副标题/类目名 LIKE）

装修 floorsJson ──► AllowedFloorTypes + sortOrder 规范化（禁止 LIVE）

站内通知 notifications ──► /notifications（列表/已读）· /admin/notifications（写入）
```

## 1. 平台券

| 项 | 说明 |
|----|------|
| 发放 | `POST /api/v1/admin/platform-coupons` |
| 公开列表 | `GET /api/v1/platform-coupons` |
| 领取 | `POST /api/v1/buyer/platform-coupons/{id}/claim` |
| 我的领取 | `GET /api/v1/buyer/platform-coupons/claims` |
| 下单 | `CheckoutRequest.platformCouponClaimId` |

### 与店券规则（定稿）

- **默认 `MUTUAL_EXCLUSIVE`**（`meiyue.coupon.stacking`）：同一订单**不可**同时传 `storeCouponClaimId`（或旧字段 `couponClaimId`）与 `platformCouponClaimId`。
- 店券：仅单店订单，按该店行小计校验门槛。
- 平台券：按整单小计校验门槛后抵扣。
- `STACKABLE` 预留，本迭代不启用。
- 单测：`CouponStackingRulesTest`。

## 2. 商品评价

| 项 | 说明 |
|----|------|
| 前置 | 订单状态 `COMPLETED`（买家 `POST .../confirm-receipt`） |
| 提交 | `POST /api/v1/buyer/reviews`（orderId + orderItemId + rating + content） |
| 公开 | `GET /api/v1/products/{productId}/reviews` |
| 商家 | `GET /api/v1/seller/reviews` · `POST .../reply` |
| 约束 | 同一 `order_item_id` 仅一条 |

实现落在 **trade** 模块（避免 catalog↔trade 循环依赖）。

## 3. 基础搜索

- `GET /api/v1/products?q=关键词&categoryId=`
- DB LIKE：标题、副标题、类目名；不上 OpenSearch。

## 4. 装修增强

- 模板默认楼层增加 `CATEGORY_NAV` / `COUPON_ENTRY` / `IMAGE_STRIP` / `RICH_TEXT` 等，并带 `sortOrder`。
- 保存/发布：白名单校验 + 按 `sortOrder` 排序写回。
- **禁止** `LIVE` / `LIVE_STREAM` /「直播」字样。

## 5. 站内通知骨架

| API | 说明 |
|-----|------|
| `GET /notifications` | 我的列表 |
| `GET /notifications/unread-count` | 未读数 |
| `POST /notifications/{id}/read` | 标已读 |
| `POST /notifications/read-all` | 全部已读 |
| `POST /admin/notifications` | 平台写入联调 |

非推送 / 非 IM。模块：`meiyue-support`。

## 前端入口

| 端 | 能力 |
|----|------|
| buyer | 搜索、领平台券、互斥选券下单、确认收货评价、通知列表 |
| seller | `/reviews` 回复；装修文案更新 |
| admin | 平台发券、写通知 |

## 不做

- AI 视频、官方分账打款、OpenSearch、自由拖拽装修、直播组件。
