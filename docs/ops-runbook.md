# 美月商城 · 运维手册（I12）

> 范围：本地/联调启动、健康检查、备份、密钥环境变量清单。  
> **禁止**将真实密钥提交入库；官方分账打款本迭代不做。

---

## 1. 启动

### 依赖（Docker）

```bash
cd docker
docker compose up -d
docker compose ps
# postgres healthy :5432 · redis healthy :6379
```

无 Docker 时可本机安装 PostgreSQL 16 / Redis 7，库名用户密码见 `application.yml` 默认值。

### API

```bash
cd backend
mvn -DskipTests package
java -jar meiyue-boot/target/meiyue-boot-0.1.0-SNAPSHOT.jar
# 默认 :8080
```

### 前端（可选）

```bash
cd frontend
pnpm install
pnpm --filter web-buyer dev    # :5173
pnpm --filter web-seller dev   # :5174
pnpm --filter web-admin dev    # :5175
```

### 端到端冒烟

```bash
# 先确保 compose + API 已起
chmod +x scripts/smoke-e2e.sh
BASE_URL=http://localhost:8080 ./scripts/smoke-e2e.sh
```

种子账号：`admin` / `admin123`（启动时 `PlatformAdminSeeder` 写入）。

---

## 2. 健康检查

| 检查 | 命令 / 路径 |
|------|-------------|
| 存活 | `GET /actuator/health` → `{"status":"UP"}` |
| 信息 | `GET /actuator/info` |
| 指标 | `GET /actuator/metrics`（需认证） |
| Prometheus | `GET /actuator/prometheus`（需认证） |
| DB | `pg_isready -h 127.0.0.1 -U meiyue -d meiyue_mall` |
| Redis | `redis-cli ping` → `PONG` |
| Flyway | 日志含 `Successfully applied` / `now at version v12` |

---

## 3. 密钥与环境变量清单（勿入库明文）

| 变量 / 配置键 | 用途 | 本地默认 |
|---------------|------|----------|
| `meiyue.security.jwt.secret` | JWT 签名 | 开发占位串（生产必换） |
| `MEIYUE_WECHAT_APP_ID` / `MCH_ID` / `API_V3_KEY` / `PRIVATE_KEY_PEM` | 微信 | 空 |
| `MEIYUE_WECHAT_NOTIFY_URL` | 微信回调 URL | localhost 占位 |
| `MEIYUE_ALIPAY_APP_ID` / `PRIVATE_KEY` / `ALIPAY_PUBLIC_KEY` | 支付宝 | 空 |
| `MEIYUE_ALIPAY_NOTIFY_URL` | 支付宝回调 | localhost 占位 |
| `MEIYUE_AI_PROVIDER` | AI 通道 `MOCK`/`OPENAI_COMPAT` | `MOCK` |
| `MEIYUE_AI_API_KEY` / `BASE_URL` / `MODEL` | OpenAI 兼容 | 空 |
| `spring.datasource.*` | DB | `meiyue`/`meiyue` |
| `spring.data.redis.*` | Redis | localhost:6379 |

支付 / AI 真实密钥**仅**环境变量或密钥管理注入；仓库与 Flyway **不存**明文密钥。

常用业务开关：

| 键 | 含义 |
|----|------|
| `meiyue.payment.mock-enabled` | 允许 MOCK 支付 |
| `meiyue.payment.default-channel` | 默认 `MOCK` |
| `meiyue.notify.events-enabled` | 站内通知事件 |
| `meiyue.coupon.stacking` | `MUTUAL_EXCLUSIVE` |
| `meiyue.jobs.db-fallback-enabled` | 定时任务 DB 兜底 |

---

## 4. 备份与恢复

见 `docs/backup-restore.md`（`pg_dump` / 旁路库演练）。  
恢复后须重新注入上述密钥环境变量。

---

## 5. 常见故障

| 现象 | 处理 |
|------|------|
| Flyway 校验失败 | 勿手改已应用迁移；新变更用新版本号 |
| Redis 连不上 | 应用可降级部分能力；建议拉起 compose redis |
| MOCK 退款失败 | 确认订单已 `mock-pay` 成功；`payment_refunds` 按售后幂等 |
| 微信/支付宝调用报未对接 | 预期：本地用 MOCK；真实通道需配置 + SDK |

---

## 6. 关系简图

```
docker-compose (PG+Redis)
        │
        ▼
  meiyue-boot :8080 ──actuator──► 运维探针
        │
        ├── JWT / 支付 / AI 密钥 ← 环境变量（不入库）
        └── scripts/smoke-e2e.sh ──联调冒烟
```
