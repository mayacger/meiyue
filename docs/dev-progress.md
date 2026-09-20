# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I12 已完成**（持续开发授权 C19；I12 为质量加固）  
> 仓库：`mayacger/meiyue` · 分支：`cursor/meiyue-mall-scaffold-9727` · PR #1  
> 规划：`ecommerce-platform-plan.md` / `project-context.md` **v1.2 + C19**  
> 详档：`docs/i10-phase2-capabilities.md` · `docs/i11-capabilities.md` · `docs/ops-runbook.md`

---

## 入口

| 入口 | 说明 |
|------|------|
| API | `backend/meiyue-boot` → `:8080` |
| 商家端 | `frontend/apps/web-seller` → `:5174` |
| 平台端 | `frontend/apps/web-admin` → `:5175` |
| 买家端 | `frontend/apps/web-buyer` → `:5173` |
| Compose | `docker/docker-compose.yml`（PostgreSQL + Redis） |
| 冒烟 | `scripts/smoke-e2e.sh` |
| 运维 | `docs/ops-runbook.md` |

种子：`admin`/`admin123`（启动种子）；冒烟脚本自注册买卖家

---

## I1–I11（摘要）

- I1–I8 入驻到 AI 图/详情 · I9 Redis/店券/结算 · I10 平台券/评价/搜索/装修/通知骨架 · I11 通知投递/装修可视化/面单/推广视频 MOCK  
- 详阅历史章节与模块 README

---

## I12 已交付（质量加固）

### 端到端冒烟

- `scripts/smoke-e2e.sh`：注册 → 入驻审核 → 上架 → 加购下单 → MOCK 支付 → 发货 → 确认收货 → 评价 → 售后同意（触发 MOCK 退款）
- 前置：`docker compose` + API 启动（见脚本头注释 / `ops-runbook`）

### 通道退款占位

- Flyway `V12`：`payment_refunds`（`aftersale_id` 唯一幂等）
- `PaymentChannelClient#refund`：MOCK 成功；WECHAT/ALIPAY 接口保留未对接
- 售后入账时先 `PaymentService.refundByAftersale` 再记结算账本

### 跨店券规则

- **店券**：仅单店（`CouponStackingRules.assertStoreCouponSingleShop`）
- **平台券**：可用于跨店整单；与店券默认互斥
- 单测：`CouponStackingRulesTest`（含跨店断言）

### 运维手册

- `docs/ops-runbook.md`：启动、健康检查、密钥环境变量清单、备份入口

### I12 验证

```bash
docker compose -f docker/docker-compose.yml up -d
cd backend && mvn -q -DskipTests package
# 单测
mvn -q -pl meiyue-trade,meiyue-payment -am test \
  -Dtest=CouponStackingRulesTest,MockPaymentChannelClientTest \
  -Dsurefire.failIfNoSpecifiedTests=false
java -jar meiyue-boot/target/meiyue-boot-0.1.0-SNAPSHOT.jar &
BASE_URL=http://localhost:8080 ./scripts/smoke-e2e.sh
```

---

## 下一步

- 真实微信/支付宝退款与支付 SDK；AI 外呼与视频模型
- 真实电子面单；通知推送通道
- 官方分账打款（需商户资质；本仓库仅账本）

---

## 已知限制

- 店券不可跨店；平台券可跨店但与店券互斥
- 微信/支付宝退款接口为占位（未配置密钥时不可用）
- 面单 / 推广视频 / 支付默认 MOCK
- 无官方分账打款；密钥禁止入库
