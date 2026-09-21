# meiyue-logistics（I5 / I6 逆向部分）

## 职责

正向发货状态机、运单轨迹、逆向运单创建（供售后模块调用）。轨迹查询通过 `ExpressTrackQueryPort` 抽象，当前实现为占位。

## 入口

| HTTP | 角色 | 说明 |
|------|------|------|
| `POST /api/v1/seller/shipments` | 商家 | 创建正向运单（PAID/FULFILLING） |
| `GET /api/v1/seller/shipments` | 商家 | 本店正向运单列表 |
| `POST /api/v1/seller/shipments/{id}/status` | 商家 | 手工推进状态 |
| `POST /api/v1/seller/shipments/{id}/sync-tracks` | 商家 | 占位轨迹查询并尝试推进状态 |
| `GET /api/v1/buyer/orders/{orderId}/shipments` | 登录买家 | 按订单查运单+轨迹 |

内部 API（非 HTTP）：`LogisticsService#createReverse` / `confirmReverseDelivered` 由 `meiyue-aftersale` 调用。

## 关系图

```
Order(PAID)
  └─ createForward → Shipment(FORWARD, PENDING_PICKUP) + Track
       ├─ sync-tracks / status → … → DELIVERED
       └─ 全部 FORWARD 签收 → Order.COMPLETED

Aftersale(APPROVED, RETURN_REFUND)
  └─ createReverse → Shipment(REVERSE) → confirmReverseDelivered
```

## 关键类型字段

### Shipment

| 字段 | 含义 |
|------|------|
| tenantId | 店铺租户 |
| orderId / orderItemId | 关联订单（行可选） |
| direction | FORWARD \| REVERSE |
| carrierCode / trackingNo | 承运商与运单号 |
| status | 正向/逆向枚举名 |
| aftersaleId | 逆向时挂售后单 |
| receiver* | 收件摘要 |

### ShipmentTrack

| 字段 | 含义 |
|------|------|
| shipmentId | 运单 |
| status / description | 节点状态与文案 |
| source | MANUAL \| QUERY |
| trackedAt | 节点时间 |

## 正向状态机

`PENDING_PICKUP → PICKED_UP → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED`  
分支：`EXCEPTION` / `REJECTED` / `RETURNED_TO_SENDER`（见 `ForwardStatus#canTransitTo`）。

## 迁移

`V5__i5_forward_logistics.sql` → `shipments` / `shipment_tracks`。

## 已知限制

- 未接真实快递开放平台；`PlaceholderExpressTrackQuery` 仅返回揽收+运输中模拟节点
- 不支持多包裹拆合的复杂规则（MVP：一单可多运单，全部签收才 COMPLETED）
