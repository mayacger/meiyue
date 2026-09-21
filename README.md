# 美月商城（Meiyue Mall）

> B2B2C 多租户电商平台 · 中国大陆 · 工程标识 `meiyue-mall`  
> 仓库：`mayacger/meiyue`（前后端同仓）  
> 规划定稿：见项目 Context 中 `ecommerce-platform-plan.md` **v1.2**  
> 进度：`docs/dev-progress.md`（**I1→I14**；C19+C20；前端四端见 `frontend/README.md`）

---

## 本仓库目录（脚手架）

```text
.
├── backend/                 # Maven 多模块 · Java 21 + Spring Boot 3
│   ├── meiyue-boot/         # API 启动（组装各域）
│   ├── meiyue-worker/       # Worker 进程空壳
│   ├── meiyue-common/       # 租户上下文 / 安全常量 / 错误码
│   ├── meiyue-identity/     # IAM：JWT / RBAC（I1）
│   ├── meiyue-tenant/       # 入驻审核开店（I1）
│   ├── meiyue-catalog/      # 类目 / SPU·SKU（I2）
│   ├── meiyue-decoration/   # 模板装修（I2）
│   └── meiyue-payment/      # 微信/支付宝通道占位
├── frontend/                # pnpm monorepo · React + Taro
│   ├── apps/web-buyer       # 买家 PC（设计向）
│   ├── apps/web-seller      # 商家后台（Ant Design Pro）
│   ├── apps/web-admin       # 平台后台（Ant Design Pro）
│   ├── apps/taro-buyer      # 买家移动（Taro H5+weapp）
│   └── packages/ui|types|api
├── docs/                    # scaffold-progress / dev-progress
└── docker/                  # docker-compose（PostgreSQL + Redis）
```

---

## 模块关系图（简要）

```text
                    ┌─────────────┐
                    │ meiyue-boot │  ← API :8080
                    └──────┬──────┘
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
     identity/tenant   业务域占位      meiyue-common
     (I1 填充)        (catalog…)     TenantContext
                                         ▲
                    ┌─────────────┐      │
                    │meiyue-worker│ 空壳  │
                    └─────────────┘      │
                                         │
  web-buyer / web-seller / web-admin / taro-buyer ─── /api proxy
```

与规划文档对应：

| 规划章节 | 本仓库落地 |
|----------|------------|
| §6.1 目录 | `backend/*` + `frontend/*` |
| §5.4 技术栈 | Java 21 / Boot 3 / Security / JPA 依赖占位 / pnpm React |
| §3 多租户 | `TenantContext` + `TenantContextFilter` 骨架 |
| §5.3 支付 | `PaymentChannel` + `PlaceholderPaymentGateway` |
| 迭代 I1+ | 见 Context `scaffold-progress.md` |

---

## 环境要求

| 组件 | 版本 |
|------|------|
| JDK | **21**（LTS，必须） |
| Maven | 3.8+ |
| Node.js | **≥ 20** |
| pnpm | **≥ 9**（仓库约定） |

PostgreSQL：本地或 `docker compose -f docker/docker-compose.yml up -d`。  
Redis：I9 已接入（关单/售后延迟、限流、可选 AI 队列）；`docker compose` 默认含 Redis，或系统 `redis-server`。DB 扫描兜底见 `meiyue.jobs.db-fallback-enabled`。

---

## 后端本地启动

```bash
cd backend
mvn -q -DskipTests package

# API
java -jar meiyue-boot/target/meiyue-boot-0.1.0-SNAPSHOT.jar
# 探活
curl http://localhost:8080/api/v1/ping
curl http://localhost:8080/actuator/health

# Worker（可选，端口 8081）
java -jar meiyue-worker/target/meiyue-worker-0.1.0-SNAPSHOT.jar
```

---

## 前端本地启动

```bash
cd frontend
pnpm install
pnpm --filter @meiyue/web-buyer build   # 验证至少一个 app 可构建
pnpm dev:buyer    # http://localhost:5173
pnpm dev:seller   # http://localhost:5174
pnpm dev:admin    # http://localhost:5175
```

---

## 明确不做（脚手架与后续均遵守）

- 直播 / 短视频带货  
- 本轮真实微信/支付宝对接  
- 完整订单 / 物流业务（属 I3–I6）  
- NestJS / Node 作为 API 后端  

---

## 文档

- 仓内：`backend/README.md`、`frontend/README.md`  
- 项目 Context：`docs/scaffold-progress.md`（进度与下一步 I1/I2）  
- 权威规划：项目 Agent Store 中 `ecommerce-platform-plan.md` / `project-context.md` v1.2  
