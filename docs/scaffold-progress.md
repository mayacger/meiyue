# 美月商城 · 脚手架进度说明（用户可读）

> 状态：**脚手架已开工落地**（最小可运行底座）  
> 对应规划：`ecommerce-platform-plan.md` / `project-context.md` **v1.2**  
> 仓库：`mayacger/meiyue`（前后端同仓）  
> 更新：脚手架首轮（开工）

---

## 1. 入口一览

| 入口 | 路径 / 命令 | 说明 |
|------|-------------|------|
| 根说明 | 仓库 `README.md` | 如何启动、目录树、与规划对应 |
| API 启动类 | `backend/meiyue-boot/.../MeiyueBootApplication.java` | 端口 8080；`GET /api/v1/ping`、`/actuator/health` |
| Worker 启动类 | `backend/meiyue-worker/.../MeiyueWorkerApplication.java` | 端口 8081；仅健康检查空壳 |
| 买家端 | `frontend/apps/web-buyer` | `pnpm dev:buyer` → :5173 |
| 商家端 | `frontend/apps/web-seller` | `pnpm dev:seller` → :5174 |
| 平台端 | `frontend/apps/web-admin` | `pnpm dev:admin` → :5175 |
| 后端模块说明 | `backend/README.md` | Maven 模块关系 |
| 前端模块说明 | `frontend/README.md` | pnpm workspace 关系 |

权威产品/架构定稿仍以本 Context 下规划文档为准，不以脚手架代码为准覆盖决策。

---

## 2. 目录关系图

```text
mayacger/meiyue（仓库根 = 工程 meiyue-mall）
│
├── backend/                          Maven 父工程 meiyue-mall-parent
│   ├── meiyue-common                 TenantContext / Filter / SecurityConstants
│   ├── meiyue-identity               IAM 空壳 → I1
│   ├── meiyue-tenant                 租户/店铺空壳 → I1
│   ├── meiyue-catalog|decoration|…   域占位 → I2+
│   ├── meiyue-payment                微信/支付宝 Placeholder（无真实对接）
│   ├── meiyue-boot                   组装启动 API
│   └── meiyue-worker                 异步任务进程空壳
│
└── frontend/                         pnpm workspace
    ├── apps/web-buyer|seller|admin   三端路由壳 + 首页
    └── packages/ui|types             最小共享
```

```text
请求链路（脚手架）

浏览器 → web-* (Vite)
           │ proxy /api
           ▼
       meiyue-boot
           │ TenantContextFilter → TenantContext(ThreadLocal)
           │ SecurityFilterChain（公开 ping/health；其余暂放行）
           ▼
       PingController / Actuator
```

---

## 3. 本轮已交付

1. **后端**：Java 21 + Spring Boot 3.3 多模块；Security 基线；Actuator 健康检查；`tenant_id` 过滤器骨架；PG/Redis `application.yml` 占位；启动期 exclude 数据源/Redis 以便无中间件可跑。  
2. **前端**：pnpm monorepo；三端最小页面 + 路由壳；共享 `ui`/`types`。  
3. **支付**：仅枚举 + Placeholder 接口，**无**微信/支付宝真实对接。  
4. **文档**：根 README、backend/frontend README、本文。  
5. **遵守**：无直播；无完整订单/物流实现。

---

## 4. 已知限制

- 未连接真实 PostgreSQL / Redis（自动配置已 exclude）。  
- Security 尚未接 JWT/登录；业务 API 暂 `permitAll`（仅脚手架）。  
- 租户头 `X-Tenant-Id` 仅联调演示，**生产不可信**（I1 改为鉴权注入）。  
- 域模块多为 `package-info` 占位，无表结构 / Flyway。  
- 前端无 Ant Design 管理端皮肤（I1/I2 再引入）。  
- Worker 无任何 Job。

---

## 5. 下一步

### I1 — 登录、入驻审核、开店

- `meiyue-identity`：账号、BCrypt、JWT/Session、RBAC（买家/商家/平台）  
- `meiyue-tenant`：入驻审核状态机、开店（1:1 Store）  
- 启用 DataSource + Flyway；租户上下文改为鉴权写入  
- 三端登录页与路由守卫  

### I2 — 商品发布、买家浏览、模板装修

- `meiyue-catalog`：SPU/SKU、类目、上下架  
- `meiyue-decoration`：模板 + 楼层 + 主题色 + 草稿/发布（**无直播楼层**）  
- 买家浏览已发布店铺页 / 商品详情  

其后按规划：I3 交易模拟支付 → I4 真实支付 → I5/I6 物流售后 → I7 可观测与串租 → I8 AI 图/详情。

---

## 6. 构建自检（脚手架验收）

```bash
# 后端（需 JDK 21）
cd backend && mvn -q -DskipTests package

# 前端
cd frontend && pnpm install && pnpm --filter @meiyue/web-buyer build
```
