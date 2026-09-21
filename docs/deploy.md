# 美月商城 · 生产部署（I34）

> 范围：容器镜像、compose prod profile、环境变量、健康检查。  
> **禁止**将真实 JWT / 支付 / AI 密钥提交入库。不含直播、真实分账打款。

## 1. 架构

```
浏览器 → web-buyer(nginx:80) ─┬─ /           静态 SPA
                              └─ /api/*      proxy → api:8080
api(jar:8080) → postgres / redis
```

## 2. 构建与启动

```bash
# 依赖：已有 docker/docker-compose.yml（postgres + redis）
cp docker/.env.prod.example docker/.env.prod
# 编辑 docker/.env.prod：POSTGRES_PASSWORD、MEIYUE_SECURITY_JWT_SECRET（≥32 字符）

docker compose \
  -f docker/docker-compose.yml \
  -f docker/docker-compose.prod.yml \
  --env-file docker/.env.prod \
  up -d --build
```

健康检查：

```bash
curl -sf http://localhost:8080/actuator/health   # API → {"status":"UP"}
curl -sf http://localhost:8088/                  # 买家静态站
```

单独构建 API：

```bash
docker build -t meiyue-api:local -f backend/Dockerfile backend
```

单独构建买家前端：

```bash
docker build -t meiyue-web-buyer:local -f frontend/Dockerfile.buyer frontend
```

## 3. 环境变量（API）

| 变量 | 说明 | 默认 |
|------|------|------|
| `SPRING_DATASOURCE_URL` | JDBC | compose 内 `jdbc:postgresql://postgres:5432/...` |
| `SPRING_DATASOURCE_USERNAME` / `PASSWORD` | 库账号 | 必填密码 |
| `SPRING_DATA_REDIS_HOST` | Redis | `redis` |
| `MEIYUE_SECURITY_JWT_SECRET` | JWT 签名密钥（≥32） | **必填** |
| `MEIYUE_SECURITY_CAPTCHA_ENABLED` | I35 登录图形验证码开关 | `false` |
| `MEIYUE_PAYMENT_DEFAULT_CHANNEL` | `MOCK` / `WECHAT` / `ALIPAY` | `MOCK` |
| `MEIYUE_WECHAT_*` / `MEIYUE_ALIPAY_*` | 支付密钥 | 空（不入库） |
| `MEIYUE_AI_PROVIDER` / `MEIYUE_AI_API_KEY` | AI 出图 | `MOCK` / 空 |
| `SPRING_PROFILES_ACTIVE` | 勿用 `demo` 上生产 | 空 |

Spring Boot 亦支持标准 `SPRING_DATASOURCE_*` 覆盖 `application.yml`。

## 4. 本地非容器（对照）

见 [ops-runbook.md](./ops-runbook.md)：`mvn package` + `java -jar` + `pnpm build`。

## 5. 冒烟

```bash
BASE_URL=http://localhost:8080 ./scripts/smoke-e2e.sh
BASE_URL=http://localhost:8080 ./scripts/smoke-i18.sh
```
