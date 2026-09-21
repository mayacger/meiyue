# I26 / I27 能力说明

> 入口：同一 PR · 无新增 Flyway  
> **不做**：直播、真实分账、密钥联调

## 关系图

```
店主 SELLER_OWNER
  ├── POST /seller/staff/invite ──► SellerMember(STAFF) + Role SELLER_STAFF
  ├── GET/DELETE /seller/staff
  ├── PUT /seller/store · GET /seller/settlements/*（仅 OWNER）
  └── 商品/发货/库存/概览（OWNER + STAFF）

springdoc（demo/dev）
  └── /v3/api-docs · /swagger-ui.html

Dashboard series（I27）
  ├── GET /seller/dashboard/series?days=7
  └── GET /admin/dashboard/series?days=7
        └── Recharts 折线/柱状（Seller/Admin 概览）

GET /seller/products/drafts ──► ProductStatus.DRAFT 草稿箱 Tab
```

## I26

| 能力 | 端点 / 入口 |
|------|-------------|
| 员工列表 | `GET /api/v1/seller/staff` · Seller `/staff` |
| 邀请店员 | `POST /api/v1/seller/staff/invite` `{username}` |
| 移除店员 | `DELETE /api/v1/seller/staff/{memberId}` |
| 权限收紧 | 结算 / 店铺 PUT / staff 仅 `SELLER_OWNER`；商品发货等 `OWNER|STAFF` |
| OpenAPI | `springdoc`：`/v3/api-docs` + swagger-ui；**仅 demo/dev profile** |

## I27

| 能力 | 端点 / 入口 |
|------|-------------|
| 商家序列 | `GET /api/v1/seller/dashboard/series?days=7` · Seller 概览折线图 |
| 平台序列 | `GET /api/v1/admin/dashboard/series?days=7` · Admin 概览柱状图 |
| 草稿箱 | `GET /api/v1/seller/products/drafts` · 商品页「草稿箱」Tab |

## 冒烟

`scripts/smoke-e2e.sh`、`scripts/smoke-i18.sh` 已覆盖 I26/I27（OpenAPI、员工邀请与 403、series、drafts）。
