# 美月商城 · I13 工程化收尾

> 范围：CI、演示种子、文档/PR 说明。  
> **不做**真实支付联调、真实分账、密钥入库。

---

## 入口

| 入口 | 路径 / 命令 |
|------|-------------|
| CI 工作流 | `.github/workflows/ci.yml` |
| Demo 配置 | `backend/meiyue-boot/src/main/resources/application-demo.yml` |
| Demo 种子类 | `com.meiyuemall.boot.config.DemoSeeder` |
| Demo 说明 | `backend/meiyue-boot/src/main/resources/db/demo/README.md` |
| 变更摘要 | 仓库根 `CHANGELOG.md` |
| 运维 | `docs/ops-runbook.md` |

---

## 关系图

```
GitHub Actions
├── job backend  → mvn package + 离线单测
└── job frontend → pnpm install + build 三端

meiyue-boot 启动
├── PlatformAdminSeeder（始终）
└── DemoSeeder（meiyue.demo.enabled=true）
        └── 用户 / 租户店 / 商品 / 已发布装修
```

---

## CI 说明

- **不**启动 PostgreSQL/Redis 集成测试（避免 runner 依赖与外网支付）。
- 单测仅：`CouponStackingRulesTest`、`MockPaymentChannelClientTest`。
- 前端使用 `pnpm@9` + Node 20，与 `packageManager` 字段一致。

---

## Demo 账号（仅 profile=demo）

| 用户 | 密码 | 用途 |
|------|------|------|
| admin | admin123 | 平台（始终种子） |
| seller1 | seller123 | 商家端演示 |
| buyer1 | buyer123 | 买家端演示 |

店铺 slug：`demo-flower`。

启用：

```bash
java -jar meiyue-boot/target/meiyue-boot-0.1.0-SNAPSHOT.jar \
  --spring.profiles.active=demo
```

---

## 与 I12 冒烟的关系

- `scripts/smoke-e2e.sh` **自注册**买卖家，不依赖 DemoSeeder。
- Demo 种子用于人工开箱浏览三端；冒烟用于回归交易闭环。
