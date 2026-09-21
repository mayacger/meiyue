# I36–I38 能力说明

> 同一 PR · Flyway **V21** · 无直播、无真实分账  

## 关系图

```
I36 软删
  products.deleted_at
       ├── Seller DELETE /seller/products/{id} → 回收站
       ├── Seller POST …/restore · GET …/deleted
       └── Admin GET/POST /admin/products/deleted|…/restore

I36 销售报表
  DashboardAggregateService.export*SalesCsv(grain, periods)
       ├── GET /seller/dashboard/sales-report.csv
       └── GET /admin/dashboard/sales-report.csv
            columns: period,orderCount,gmvCents,refundCents

I37 a11y / 静态 / Taro
  Buyer :focus-visible · 页脚 /about /help
  docs/taro-bundle.md · optimizeMainPackage

I38 收敛
  docs/credentials-backlog.md（待密钥联调清单）
```

## 冒烟

`smoke-e2e.sh` / `smoke-i18.sh` 覆盖软删恢复、销售 CSV 头字段。
