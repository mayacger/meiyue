# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I13 已完成**（持续开发授权 C19；I13 为工程化收尾）  
> 仓库：`mayacger/meiyue` · 分支：`cursor/meiyue-mall-scaffold-9727` · PR #1  
> 规划：`ecommerce-platform-plan.md` / `project-context.md` **v1.2 + C19**  
> 详档：`docs/i10-phase2-capabilities.md` · `docs/i11-capabilities.md` · `docs/i13-engineering.md` · `docs/ops-runbook.md` · `CHANGELOG.md`

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
| CI | `.github/workflows/ci.yml` |
| 运维 | `docs/ops-runbook.md` |

种子：`admin`/`admin123`（始终）；`demo` profile 另有 `seller1`/`seller123`、`buyer1`/`buyer123`；冒烟脚本自注册买卖家

---

## I1–I12（摘要）

- I1–I8 入驻到 AI 图/详情 · I9 Redis/店券/结算 · I10 平台券/评价/搜索/装修/通知骨架 · I11 通知投递/装修可视化/面单/推广视频 MOCK · I12 冒烟/MOCK 退款/跨店券/运维手册  
- 详阅历史章节与 `CHANGELOG.md`

---

## I13 已交付（工程化收尾）

### GitHub Actions CI

- Backend：`mvn -B -DskipTests package` + 关键离线单测（券叠加 / MOCK 退款）
- Frontend：`pnpm install --frozen-lockfile` + `pnpm build` 三端
- 不依赖真实商户密钥与外网支付联调

### 演示种子（可开关）

- `application-demo.yml` + `meiyue.demo.enabled`
- `DemoSeeder`：示例店 `demo-flower`、2 件上架商品、已发布装修页
- 说明：`docs/i13-engineering.md` · `db/demo/README.md`

### 文档

- 根目录 `CHANGELOG.md`：I1–I13 能力、限制、smoke 步骤
- 本文件与 store 进度同步更新

### I13 验证

```bash
# CI 等价
cd backend && mvn -B -DskipTests package \
  && mvn -B -pl meiyue-trade,meiyue-payment -am test \
     -Dtest=CouponStackingRulesTest,MockPaymentChannelClientTest \
     -Dsurefire.failIfNoSpecifiedTests=false
cd frontend && pnpm install --frozen-lockfile && pnpm build

# 可选：demo 种子启动后人工浏览；冒烟仍用脚本自注册
docker compose -f docker/docker-compose.yml up -d
java -jar backend/meiyue-boot/target/meiyue-boot-0.1.0-SNAPSHOT.jar --spring.profiles.active=demo
BASE_URL=http://localhost:8080 ./scripts/smoke-e2e.sh
```

---

## 下一步（暂停大功能，待密钥）

- 真实微信/支付宝支付与退款 SDK（需用户提供商户密钥）
- AI 外呼与视频模型（需 API Key）
- 真实电子面单；通知推送通道
- 官方分账打款（需商户资质；本仓库仅账本，**不做**）

---

## 已知限制

- 店券不可跨店；平台券可跨店但与店券互斥
- 微信/支付宝支付/退款为占位（未配置密钥时不可用）
- 面单 / 推广视频 / 支付默认 MOCK
- 无官方分账打款；密钥禁止入库
- Demo 种子仅本地开箱，生产勿开 `demo` profile
