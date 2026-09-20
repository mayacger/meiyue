# 美月商城 · 开发进度（用户可读）

> 状态：**I1 + I2 + I3 已完成**（持续开发授权 C19，无需逐步确认）  
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

种子账号：`admin` / `admin123`（PLATFORM_ADMIN）

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

## 下一步

- **I4**：微信/支付宝真实对接、幂等、查单、日对账  
- **I5/I6**：正逆向物流  

---

## 已知限制

- Redis 仍 exclude（超时关单用 DB 扫描）
- 装修为 JSON 编辑非拖拽
- JWT secret / MOCK 支付仅开发用途
- 真实支付未对接（I4）
