# I12 · 跨店券与退款说明

## 跨店券（定稿）

| 券类型 | 单店订单 | 跨店订单 | 与另一类券 |
|--------|----------|----------|------------|
| 店券 | ✅ 按该店行小计 | ❌ 拒绝 | 默认互斥 |
| 平台券 | ✅ 按整单小计 | ✅ 按整单小计 | 默认互斥 |

实现：`CouponStackingRules` + `OrderService` 下单路径。  
单测：`CouponStackingRulesTest`。

## 通道退款

售后同意 / 仅退款关闭时：

1. `PaymentService.refundByAftersale(orderId, aftersaleId, amount)`  
2. 按 `aftersale_id` 查 `payment_refunds` → 已有则幂等返回  
3. `PaymentChannelClient.refund`（MOCK 立即成功）  
4. `SettlementLedgerService.recordRefund` 记账  

WECHAT / ALIPAY：`refund` 方法已留，返回 `*_REFUND_NOT_IMPLEMENTED`。
