# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I8 已完成**（持续开发授权 C19，无需逐步确认）  
> 仓库：`mayacger/meiyue` · 分支：`cursor/meiyue-mall-scaffold-9727` · PR #1  
> 规划：`ecommerce-platform-plan.md` / `project-context.md` **v1.2 + C19**

---

## 入口

| 入口 | 说明 |
|------|------|
| API | `backend/meiyue-boot` → `:8080` |
| 商家端 | `frontend/apps/web-seller` → `:5174` |
| 平台端 | `frontend/apps/web-admin` → `:5175` |
| 买家端 | `frontend/apps/web-buyer` → `:5173` |
| Compose | `docker/docker-compose.yml`（本环境亦可用系统 PostgreSQL） |

种子账号：`admin` / `admin123`（PLATFORM_ADMIN）；联调常用 `seller1`/`seller123`、`buyer1`/`buyer123`

---

## I1 已交付

- JWT 注册/登录；角色 BUYER / SELLER_OWNER / SELLER_STAFF / PLATFORM_ADMIN
- RBAC（`@PreAuthorize` + SecurityFilterChain）；TenantContext 由鉴权注入（不信任客户端 tenant 头）
- 入驻申请 → 平台审核 → 自动开店（1:1 Tenant/Store）+ SellerMember
- Flyway `V1__i1_identity_tenant.sql`；本地 PostgreSQL / docker-compose
- 前端：seller 登录/注册/入驻；admin 待审通过/拒绝

### I1 验证

```bash
cd backend && mvn -q -DskipTests package
java -jar meiyue-boot/target/meiyue-boot-0.1.0-SNAPSHOT.jar
# admin 登录 → 商家注册 → apply → approve → /seller/store
```

---

## I2 已交付

- 平台类目；商品 SPU/SKU 商家 CRUD + 上/下架；买家公开浏览
- 装修：3 套模板、草稿/发布、主题色、楼层 JSON；**禁止直播楼层**
- Flyway `V2__i2_catalog_decoration.sql`
- 前端：seller 商品/装修；buyer 列表/详情/店铺页

### I2 验证

```bash
POST /api/v1/seller/products → POST .../status {"status":"ON_SALE"}
GET  /api/v1/products
PUT  /api/v1/seller/decoration/draft → POST .../publish
GET  /api/v1/stores/{tenantId}/page
```

---

## I3 已交付

- 跨店购物车；下单预占库存（悲观锁扣减）；一单多店行 + MOCK 支付单
- 模拟支付成功 → 订单 PAID；30 分钟超时 `@Scheduled` 关单回滚库存
- Flyway `V3__i3_trade_payment.sql`
- 买家端：登录 / 加购 / 结算 / 模拟支付

### I3 验证

```bash
POST /api/v1/buyer/cart/items  {"skuId":1,"quantity":2}
POST /api/v1/buyer/orders/checkout
POST /api/v1/buyer/orders/{id}/mock-pay
```

---

## I4 已交付

- 通道骨架：`MOCK` / `WECHAT` / `ALIPAY`（本地默认 MOCK）；密钥仅配置/环境变量，不入库明文
- 回调验签 + 幂等（`payment_notify_logs`）；主动查单任务；日对账任务占位
- 周期结算账本 `settlement_ledgers`（SALE/REFUND 记账；官方分账二期再接）
- Flyway `V4__i4_payment_security_settlement.sql`

### I4 验证

```bash
POST /api/v1/payments/notify/mock   # paymentNo=&channelTradeNo=&amountCents=&success=true
# 重复回调应幂等；GET /api/v1/payments/{paymentNo} 可见 SUCCESS
```

---

## I5 已交付

- 正向运单全状态：`PENDING_PICKUP → PICKED_UP → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED`（异常态 EXCEPTION/REJECTED/RETURNED_TO_SENDER）
- 轨迹表 + `PlaceholderExpressTrackQuery` 占位查询；商家可 `sync-tracks` / 手工推进状态
- 订单状态扩展：`PAID → FULFILLING → COMPLETED`（全部正向运单签收后完成）
- Flyway `V5__i5_forward_logistics.sql`
- 模块说明：`backend/meiyue-logistics/README.md`

### I5 验证

```bash
POST /api/v1/seller/shipments
POST /api/v1/seller/shipments/{id}/sync-tracks
POST /api/v1/seller/shipments/{id}/status  {"status":"OUT_FOR_DELIVERY"}
GET  /api/v1/buyer/orders/{orderId}/shipments
```

---

## I6 已交付

- 售后类型：`REFUND_ONLY` / `RETURN_REFUND`
- 流程：申请 → 审核（商家同意/拒绝；**48h 超时自动同意**）→ 仅退款直接入账关闭；退货退款填逆向运单 → 商家确认收货 → 账本 REFUND → CLOSED
- Flyway `V6__i6_aftersale_reverse.sql`；任务 `AftersaleAutoApproveJob`（60s 扫描）
- 模块说明：`backend/meiyue-aftersale/README.md`

### I6 验证

```bash
POST /api/v1/buyer/aftersales
POST /api/v1/seller/aftersales/{id}/approve
# RETURN_REFUND：
POST /api/v1/buyer/aftersales/{id}/reverse-tracking
POST /api/v1/seller/aftersales/{id}/confirm-return
# 48h：将 seller_deadline_at 回拨后等待定时任务 → status=CLOSED、reviewNote 含「48h」
```

---

## I7 已交付

- Micrometer + Actuator：`/actuator/metrics`、`/actuator/prometheus`（需登录；health/info 公开）
- 结构化日志：`logback-spring.xml` 含 `traceId` / `tenantId`；响应头 `X-Trace-Id`
- 关键接口内存限流（登录 / 支付回调 / API）；配置 `meiyue.rate-limit.*`
- 审计日志骨架：`audit_logs` + `@Audited` 切面（Flyway V7）
- 串租集成测试：`TenantIsolationIT`（跨租户更新商品 / AI 挂封面 → 404）
- 备份演练文档：`docs/backup-restore.md`

### I7 验证

```bash
curl -D- http://localhost:8080/api/v1/ping   # 看 X-Trace-Id
# 带 JWT：
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/actuator/prometheus | head
cd backend && mvn -pl meiyue-boot -am test -Dtest=TenantIsolationIT -Dsurefire.failIfNoSpecifiedTests=false
```

---

## I8 已交付

- 可插拔 Provider：`MOCK`（默认）/ `OPENAI_COMPAT`（密钥 `MEIYUE_AI_API_KEY`，无密钥失败降级）
- 素材库 `media_assets` + 内容安全占位；商品 `cover_image_url` / `cover_asset_id`
- 挂接：`POST /seller/ai/products/{id}/cover|detail`；人工：`POST /seller/ai/manual-images`
- **不做**推广视频、**不做**直播；主路径无 AI 仍可上架
- Flyway `V8__i8_ai_media_assets.sql`；模块 `backend/meiyue-ai-assist/README.md`

### I8 验证

```bash
POST /api/v1/seller/ai/images  {"prompt":"红玫瑰"}
POST /api/v1/seller/ai/details {"title":"红玫瑰","hints":"送礼"}
POST /api/v1/seller/ai/products/{id}/cover
POST /api/v1/seller/ai/manual-images {"url":"https://..."}
# provider=OPENAI_COMPAT 且无密钥 → AI_DEGRADED，改人工上传
```

---

## 下一步

- **后续**：Redis 接入 / 消息通知 / 前端发货售后与 AI 表单 / 真实微信支付宝与 AI 外呼联调
- 官方分账、AI 推广视频仍属二期

---

## 已知限制

- Redis 仍 exclude（超时关单 / 售后扫描 / 限流均为进程内或 DB）
- 装修为 JSON 编辑非拖拽
- JWT secret / MOCK 支付仅开发用途
- 微信/支付宝为对接骨架；AI OpenAI 兼容为骨架（未默认外呼）
- 轨迹查询为占位；售后退款为账本记账
- 内容安全为违禁词占位，非云审核
