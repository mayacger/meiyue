# I33 能力说明

> 入口：同一 PR · Flyway **V19**  
> **不做**：真实开票对接、直播、分账

## 关系图

```
Buyer /invoices
    └── CRUD /api/v1/buyer/invoice-profiles
            └── 结算页选用 → CheckoutRequest.invoiceTitle/TaxNo/Type
                    └── orders 发票快照字段（占位）

Checkout buyerRemark → orders.buyer_remark
订单详情展示备注与发票快照
```

## 端点

| 能力 | 端点 / 入口 |
|------|-------------|
| 抬头 CRUD | `GET/POST/PUT/DELETE /buyer/invoice-profiles` · `POST .../default` |
| 下单备注/发票 | `POST /buyer/orders/checkout` 字段 `buyerRemark` / `invoiceTitle` / `invoiceTaxNo` / `invoiceType` |

## 冒烟

`smoke-e2e.sh` / `smoke-i18.sh` 覆盖抬头 CRUD 与下单备注/发票快照断言。
