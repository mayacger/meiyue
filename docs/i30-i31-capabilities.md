# I30 / I31 能力说明

> 入口：同一 PR · Flyway **V17**（浏览足迹 / 评价隐藏 / 店铺运费 / 订单运费）  
> **不做**：直播、真实分账、复杂分区运费、密钥联调

## 关系图

```
详情页 GET /products/{id}/related
        └── 同店 ON_SALE → 不足补同类目（排除自身，limit 1–20）
                └── Buyer PC / Taro 详情「相关推荐」

登录详情 → POST /buyer/browse-history/{productId}（upsert）
我的足迹 → GET /buyer/browse-history · DELETE 清空
        └── product_browse_histories（上限 100）

Admin /reviews → GET /admin/reviews
              → POST hide / restore
公开列表 → GET /products/{id}/reviews 仅 hidden=false

Seller PUT /seller/store { freightCents, freeShippingThresholdCents }
        └── stores 运费模板
Buyer POST /buyer/orders/freight-estimate → 结算页展示
Checkout → goodsCents + freightCents = totalCents
        └── 按店：达包邮门槛运费 0，否则默认运费；多店累加
```

## I30

| 能力 | 端点 / 入口 |
|------|-------------|
| 相关推荐 | `GET /api/v1/products/{id}/related?limit=` · Buyer/Taro 详情 |
| 记足迹 | `POST /api/v1/buyer/browse-history/{productId}`（详情登录态自动） |
| 足迹列表/清空 | `GET` / `DELETE /api/v1/buyer/browse-history` · `/browse-history` · Taro「浏览足迹」 |
| 评价审核 | Admin `/reviews` · `POST .../hide|restore` |
| 公开过滤 | `GET /products/{id}/reviews` 不含已隐藏 |

## I31

| 能力 | 端点 / 入口 |
|------|-------------|
| 运费模板 | Seller `/store` · `freightCents` / `freeShippingThresholdCents`（负数清空门槛） |
| 运费预估 | `POST /api/v1/buyer/orders/freight-estimate` · Buyer 结算页 |
| 下单落库 | `Order.goodsCents` + `freightCents`；`totalCents` = 券后商品 + 运费 |

## 冒烟

`scripts/smoke-e2e.sh`、`scripts/smoke-i18.sh` 已覆盖相关推荐、足迹清空、评价隐藏/恢复、运费预估与结算字段。
