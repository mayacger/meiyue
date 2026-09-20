# I11 能力包 · 说明文档

> 入口：`backend/meiyue-boot` · Flyway `V11__i11_ewaybill_ai_video_notify.sql`

## 关系图

```
订单/支付/发货/售后 ──► NotificationPublisher (common)
                              │ meiyue.notify.events-enabled
                              ▼
                     NotificationPublisherImpl → notifications 表

Seller DecorationPage ──可视化楼层列表──► DecorationService（白名单+sortOrder）

createForward ──► package_seq 自增 + MockEwaybillProvider 打单
POST .../ewaybill/print ──► 补打面单

POST /seller/ai/videos ──► ai_video_tasks(PENDING) ──Redis──► AiTaskQueueJob
                              └── MOCK URL → media_assets(VIDEO) → products.promo_video_*
```

## 1. 通知事件自动投递

| 开关 | `meiyue.notify.events-enabled`（默认 true） |
|------|---------------------------------------------|
| 端口 | `NotificationPublisher`（common） |
| 节点 | 下单、支付成功、发货、包裹送达、确认收货、售后申请/同意/拒绝 |

关闭开关后业务主路径不受影响。

## 2. 装修可视化

- Seller 楼层列表：增删改、上移下移、主题色
- 仍非自由画布；禁止 LIVE / 直播

## 3. 电子面单 + 多包裹

| API | 说明 |
|-----|------|
| `POST /seller/shipments` | `printEwaybill`（默认 true）、`packageSeq` 可选、`orderItemId` 拆包 |
| `POST /seller/shipments/{id}/ewaybill/print` | 补打 MOCK 面单 |

字段：`package_seq` / `ewaybill_no` / `ewaybill_label_url` / `ewaybill_provider=MOCK`

## 4. AI 推广视频 MOCK

| API | 说明 |
|-----|------|
| `POST /seller/ai/videos` | 提交异步任务（可带 productId） |
| `GET /seller/ai/videos` | 任务列表 |
| `POST /seller/ai/products/{id}/promo-video` | 挂已有 VIDEO 素材 |

**不是直播**；真实模型后置；默认 MOCK。

## 不做

官方分账打款、真实快递面单、真实视频模型、直播组件。
