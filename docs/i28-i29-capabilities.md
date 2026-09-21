# I28 / I29 能力说明

> 入口：同一 PR · Flyway **V16**（Banner + 售后凭证列）  
> **不做**：直播、真实分账、密钥联调

## 关系图

```
Admin CRUD /admin/banners
        └── platform_banners
                └── GET /banners（公开）→ Buyer PC 首页 · Taro 首页 Swiper

Seller /settlements 点击周期 → bills?periodKey=
Admin /settlements 周期汇总 + bills?tenantId=&periodKey=
        └── settlement_ledgers（只读，无打款）

Buyer SeoHead → document.title / meta description / og:*
ProductDetail → og:image = coverImageUrl

申请售后 evidenceImageUrls → aftersales.evidence_image_urls
退货退款 APPROVED → POST reverse-tracking（承运商+运单号）
        └── 全站 Skeleton 加载占位
```

## I28

| 能力 | 端点 / 入口 |
|------|-------------|
| Banner CRUD | `GET/POST/PUT/DELETE /api/v1/admin/banners` · Admin `/banners` |
| 公开 Banner | `GET /api/v1/banners` · Buyer `/` · Taro 首页 |
| 商家结算明细 | 点击周期 → `GET /seller/settlements/bills?periodKey=` |
| 平台结算汇总 | `GET /admin/settlements/periods` · `/bills` · Admin `/settlements` |
| SEO | Buyer `SeoHead`：首页/列表/详情 og 友好 |

## I29

| 能力 | 端点 / 入口 |
|------|-------------|
| 凭证图 | `POST /buyer/aftersales` 字段 `evidenceImageUrls[]` |
| 退货物流 | `POST /buyer/aftersales/{id}/reverse-tracking` · 售后页承运商选择 |
| 骨架屏 | `@meiyue/ui` `Skeleton` · 首页/列表/详情/订单/售后 |

## 冒烟

`scripts/smoke-e2e.sh`、`scripts/smoke-i18.sh` 已覆盖 Banner CRUD、结算汇总、凭证图与退货物流闭环。
