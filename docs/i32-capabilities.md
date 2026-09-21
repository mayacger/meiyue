# I32 能力说明

> 入口：同一 PR · Flyway **V18**  
> **不做**：直播、真实分账

## 关系图

```
买家 POST /buyer/orders/{id}/cancel（仅 PENDING_PAYMENT）
        └── OrderService.cancelOrder（与超时关单共用）
                └── SKU stock_qty += quantity

物流全部 DELIVERED
        └── OrderService.onAllPackagesDelivered
                ├── deliveredAt / autoConfirmAt = now + N 天
                └── N≤0 → 立即 COMPLETED + 通知买家
AutoConfirmReceiptJob 扫描 autoConfirmAt 到期 → COMPLETED + 通知

搜索 → POST /buyer/search-history { keyword }
列表/清空 → GET / DELETE /buyer/search-history
        └── 未登录前端 localStorage / Taro Storage 兜底

Admin PUT /admin/platform-config { key: auto_confirm_receipt_days, value }
```

## 端点

| 能力 | 端点 / 入口 |
|------|-------------|
| 取消未支付 | `POST /api/v1/buyer/orders/{id}/cancel` · 订单列表「取消」 |
| 自动确认天数 | `platform_configs.auto_confirm_receipt_days`（默认 7）· Admin 运营配置可改 |
| 搜索历史 | `POST/GET/DELETE /api/v1/buyer/search-history` · Buyer 列表页 / Taro 首页 |

## 冒烟

`smoke-e2e.sh` / `smoke-i18.sh`：取消回滚库存、搜索清空、days=0 签收即完成。
