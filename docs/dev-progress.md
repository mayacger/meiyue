# 美月商城 · 开发进度（用户可读）

> 状态：**I1 完成 · I2 完成**（持续开发授权 C19，无需逐步确认）  
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
# 需 JDK 21 + PostgreSQL（库 meiyue_mall / 用户 meiyue）
cd backend && mvn -q -DskipTests package
java -jar meiyue-boot/target/meiyue-boot-0.1.0-SNAPSHOT.jar

curl -s -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
# 商家注册 → /seller/onboarding/apply → admin approve → /seller/store
```

---

## I2 已交付

- 平台类目；商品 SPU/SKU 商家 CRUD + 上/下架；买家公开浏览
- 装修：3 套模板、草稿/发布、主题色、楼层 JSON；**禁止直播楼层**
- Flyway `V2__i2_catalog_decoration.sql`
- 前端：seller 商品/装修页；buyer 商品列表/详情/店铺页

### I2 验证

```bash
# 登录商家后
POST /api/v1/seller/products
POST /api/v1/seller/products/{id}/status  {"status":"ON_SALE"}
GET  /api/v1/products
PUT  /api/v1/seller/decoration/draft
POST /api/v1/seller/decoration/publish
GET  /api/v1/stores/{tenantId}/page
```

---

## 下一步（按规划自动推进）

- **I3**：购物车、下单、预占库存、模拟支付  
- **I4**：微信/支付宝真实对接、幂等、查单、日对账  

---

## 已知限制

- Redis 仍 exclude（I3 预占/延迟关单再启用）
- 装修楼层为 JSON 编辑，非可视化拖拽（符合 MVP 定稿）
- 前端为最小可用页，未上 Ant Design 皮肤
- JWT secret 为开发默认值，生产须覆盖
