# 美月商城 · CHANGELOG

## [0.1.0-SNAPSHOT] · I18 后端缺口 + 四端接线（当前 PR）

- 后端：`/admin/categories` 创建/更新/启停；`/buyer/addresses` CRUD；`/admin/users` 列表+启停；Flyway V13
- Admin Pro：类目可管理；账号页对接用户目录
- 买家 PC `/addresses` + Taro 地址页对接真实 API（替换本地草稿）
- 单测：`AdminUserServiceTest` / `CategoryUpsertRequestTest`；smoke 增加 I18 片段
- 文档：`docs/dev-progress.md` · Context `frontend-architecture.md`

### 验证

```bash
cd frontend && pnpm build && pnpm build:taro:h5
pnpm --filter @meiyue/taro-buyer build:weapp
```

---

## [0.1.0-SNAPSHOT] · I17 体验打磨

- 买家 PC：售后列表 `/aftersales`、结算/订单空态统一、导航补售后入口
- 文档：admin/taro README；进度同步 I16–I17

---

## [0.1.0-SNAPSHOT] · I16 前端完整化

### 平台 / 商家 Pro

- web-admin：运营概览、类目只读、账号权限、通知受众/分类、ProLayout 角色展示
- web-seller：商品编辑 PUT、装修楼层可视化、AI 出图/详情/视频 ProForm、发货推进保留

### 买家

- web-buyer：设计 Token/动效/购物车布局/履约承诺区深化
- taro-buyer：订单列表/详情、售后申请、地址本地占位；`build:weapp` 通过

### 验证

```bash
cd frontend && pnpm build && pnpm build:taro:h5
pnpm --filter @meiyue/taro-buyer build:weapp
```

---

## [0.1.0-SNAPSHOT] · I14 前端架构重建

### 前端四端（C20）

- **web-admin / web-seller**：Ant Design Pro Components（ProLayout / ProTable / ProForm）
- **web-buyer**：设计向 PC 商城（墨绿主色、品牌全宽英雄；非 Pro 后台）
- **taro-buyer**：Taro 4 + React，目标 H5 + 微信小程序；`build:h5` 通过
- CI：`pnpm build`（三端 Web）+ `pnpm build:taro:h5`
- 文档：`frontend/README.md`、各 app README；Context `frontend-architecture.md`

### 验证

```bash
cd frontend && pnpm install && pnpm build && pnpm build:taro:h5
```

---

## [0.1.0-SNAPSHOT] · I13 工程化收尾

### 新增

- **GitHub Actions CI**（`.github/workflows/ci.yml`）
  - Backend：`mvn -B -DskipTests package` + 关键单测 `CouponStackingRulesTest` / `MockPaymentChannelClientTest`
  - Frontend：`pnpm install --frozen-lockfile` + `pnpm build`（买家/商家/平台三端）
- **演示种子**（`meiyue.demo.enabled` / profile `demo`）
  - `DemoSeeder`：buyer1、seller1、示例店 `demo-flower`、2 件上架商品、已发布装修页
  - 说明：`backend/meiyue-boot/src/main/resources/db/demo/README.md`

### I1–I12 能力摘要

| 迭代 | 能力 |
|------|------|
| I1 | 身份 / JWT / 平台管理员种子 / 模块化单体骨架 |
| I2 | 入驻审核开店、类目/商品 SKU、店铺装修模板 |
| I3 | 购物车、下单、库存预占 |
| I4 | 支付通道抽象（默认 MOCK；微信/支付宝配置位） |
| I5 | 发货 / 物流轨迹 |
| I6 | 售后（退款/退货）与结算账本骨架 |
| I7 | Actuator、限流、审计 |
| I8 | AI 出图/详情（默认 MOCK） |
| I9 | Redis 延迟队列、店券、结算增强 |
| I10 | 平台券、评价、搜索、装修/通知骨架 |
| I11 | 通知投递、装修可视化、电子面单/推广视频 MOCK |
| I12 | 冒烟脚本、MOCK 退款、跨店券规则、运维手册 |

### 已知限制

- 无真实微信/支付宝支付与退款 SDK 联调（需商户密钥）
- AI / 电子面单 / 推广视频默认 MOCK
- **无官方分账打款**（仅结算账本）
- 店券不可跨店；平台券可跨店但与店券默认互斥
- **禁止**将密钥提交入库

### 如何跑 Smoke

```bash
# 1) 依赖
docker compose -f docker/docker-compose.yml up -d

# 2) API（可选加 demo 种子）
cd backend && mvn -DskipTests package
java -jar meiyue-boot/target/meiyue-boot-0.1.0-SNAPSHOT.jar
# 演示数据：再加 --spring.profiles.active=demo
# 账号：admin/admin123；demo 下另有 seller1/seller123、buyer1/buyer123

# 3) 冒烟（自注册账号，不依赖 demo）
BASE_URL=http://localhost:8080 ./scripts/smoke-e2e.sh

# 4) CI 等价本地命令
cd backend && mvn -B -DskipTests package \
  && mvn -B -pl meiyue-trade,meiyue-payment -am test \
     -Dtest=CouponStackingRulesTest,MockPaymentChannelClientTest \
     -Dsurefire.failIfNoSpecifiedTests=false
cd frontend && pnpm install && pnpm build
```

详阅：`docs/ops-runbook.md` · `docs/dev-progress.md` · `docs/i13-engineering.md`
