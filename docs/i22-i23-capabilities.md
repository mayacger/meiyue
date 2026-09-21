# I22 / I23 能力说明

> 入口：同一 PR `cursor/meiyue-mall-scaffold-9727` · Flyway `V15__i22_store_favorites_platform_config.sql`  
> 范围：店铺设置、收藏、素材 URL 登记选用、订单 CSV、领券体验、费率只读、统一空/错态  
> **不做**：直播、真实分账打款、密钥联调、强制 OSS

## 关系图

```
Seller PUT /seller/store ──► stores(name/description/logo_url)
                                    │
Buyer GET /stores/{tenantId} ◄──────┘
Buyer GET /stores/{tenantId}/products

Buyer POST/DELETE /buyer/favorites/{productId}
       └── product_favorites ──► GET /buyer/favorites/products

Seller POST /seller/ai/manual-images (URL→media_assets)
       └── POST /seller/ai/products/{id}/cover-asset ──► products.cover_*

Seller/Admin GET …/orders/export.csv ──► 已支付订单 CSV（paidAt 非空）

GET /platform/config ──► platform_configs（费率 bps / 结算周期 / 提现门槛 · 只读）
```

## I22 API

| 端点 | 说明 |
|------|------|
| `GET/PUT /api/v1/seller/store` | 店铺资料读写 |
| `GET /api/v1/stores/{tenantId}` | 公开店页 |
| `GET /api/v1/stores/slug/{slug}` | 按 slug |
| `POST/DELETE /api/v1/buyer/favorites/{productId}` | 收藏/取消 |
| `GET /api/v1/buyer/favorites` | 收藏 ID 列表 |
| `GET /api/v1/buyer/favorites/products` | 收藏商品详情 |
| `GET /api/v1/buyer/favorites/{id}/status` | 是否已收藏 |
| `POST /api/v1/seller/ai/manual-images` | URL 登记素材（已有） |
| `POST /api/v1/seller/ai/products/{id}/cover-asset` | 选用 IMAGE 挂封面 |
| `GET /api/v1/seller/orders/export.csv` | 本店已支付 CSV |
| `GET /api/v1/admin/orders/export.csv` | 全站已支付 CSV |

## I23 前端 / 运营

| 端 | 入口 | 说明 |
|----|------|------|
| Buyer PC | `/coupons` | 平台券领取 + 券包 |
| Buyer PC | `/favorites` · `/stores/:tenantId` | 收藏 / 店页 |
| Taro | `pages/coupons` · `favorites` · `store` | 同上 |
| Admin | `/platform-config` | 费率只读 + CSV 导出 |
| Seller | `/store` · 结算页 FeeHint | 店铺设置 / 费率只读 |
| `@meiyue/ui` | `EmptyState` / `ErrorState` | 全站空态/错误态 |

## 验证

```bash
cd backend && mvn -q -DskipTests package
cd frontend && pnpm install && pnpm build
pnpm build:taro:h5 && pnpm --filter @meiyue/taro-buyer build:weapp
```
