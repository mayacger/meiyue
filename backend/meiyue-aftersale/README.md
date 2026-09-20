# meiyue-aftersale（I6）

## 职责

售后申请与审核、48 小时商家未审自动同意、仅退款 / 退货退款闭环；退款写入结算账本（非通道原路退）。

## 入口

| HTTP | 角色 | 说明 |
|------|------|------|
| `POST /api/v1/buyer/aftersales` | 买家 | 申请售后 |
| `GET /api/v1/buyer/aftersales` | 买家 | 我的售后 |
| `POST /api/v1/buyer/aftersales/{id}/reverse-tracking` | 买家 | 退货填逆向运单 |
| `GET /api/v1/seller/aftersales` | 商家 | 本店售后 |
| `POST /api/v1/seller/aftersales/{id}/approve` | 商家 | 同意 |
| `POST /api/v1/seller/aftersales/{id}/reject` | 商家 | 拒绝并关闭 |
| `POST /api/v1/seller/aftersales/{id}/confirm-return` | 商家 | 确认收到退货并退款 |

定时任务：`AftersaleAutoApproveJob`（固定延迟 60s）扫描 `seller_deadline_at < now` 且仍在审核中的单据。

## 关系图

```
Order(PAID|FULFILLING|COMPLETED)
  └─ apply → Aftersale(REVIEWING, deadline=+48h)
       ├─ reject → CLOSED
       ├─ approve + REFUND_ONLY → ledger REFUND → CLOSED
       ├─ 48h 超时 → 等同 approve
       └─ approve + RETURN_REFUND
            └─ reverse-tracking → Shipment(REVERSE)
                 └─ confirm-return → ledger REFUND → CLOSED
```

## 关键类型字段

### Aftersale

| 字段 | 含义 |
|------|------|
| aftersaleNo | 业务单号 |
| orderId / orderItemId | 关联订单行 |
| tenantId / buyerUserId | 店与买家 |
| type | REFUND_ONLY \| RETURN_REFUND |
| status | APPLIED/REVIEWING/APPROVED/REJECTED/REFUNDING/CLOSED |
| reason / refundCents | 原因与退款分 |
| sellerDeadlineAt | 审核截止（创建+48h） |
| reverseShipmentId | 逆向运单 |
| reviewNote / reviewedAt / closedAt | 审核与关闭信息 |

## 迁移

`V6__i6_aftersale_reverse.sql` → `aftersales`（可选补 `shipments.aftersale_id` FK）。

## 依赖

- `meiyue-trade`：订单校验
- `meiyue-logistics`：逆向运单
- `meiyue-payment`：`SettlementLedgerService#recordRefund`

## 已知限制

- 退款仅账本记账，未调微信/支付宝退款 API
- 库存回滚未在本迭代实现
- 自动同意依赖应用内 `@Scheduled`，多实例需后续加分布式锁/选主
