# 美月商城前端（pnpm monorepo）

## 入口

| 应用 | 包名 | 本地端口 | 技术 |
|------|------|----------|------|
| 买家 PC | `@meiyue/web-buyer` | 5173 | React + 自研设计系统（非 Pro） |
| 商家后台 | `@meiyue/web-seller` | 5174 | Ant Design Pro Components |
| 平台后台 | `@meiyue/web-admin` | 5175 | Ant Design Pro Components |
| 买家移动 | `@meiyue/taro-buyer` | 10086 (H5) | **Taro 4** · H5 + 微信小程序 |

共享包：`@meiyue/api`、`@meiyue/types`、`@meiyue/ui`。

详档：仓库外 store `docs/frontend-architecture.md`；各 app 内 `README.md`。

## 关系图

```text
frontend/
  apps/web-admin   ──Pro──► antd + pro-components + api/types
  apps/web-seller  ──Pro──► antd + pro-components + api/types
  apps/web-buyer   ──设计向──► api/types（无 antd Pro）
  apps/taro-buyer  ──Taro──► types + Taro.request
         │
         └── 开发代理 /api → http://localhost:8080
```

## 本地启动

```bash
cd frontend
pnpm install
pnpm build                 # CI：admin / seller / buyer
pnpm build:taro:h5         # Taro H5（可选）
pnpm dev:buyer             # PC 商城
pnpm dev:seller            # 商家 Pro
pnpm dev:admin             # 平台 Pro
pnpm dev:taro:h5           # 移动 H5
```

## 品牌

美月商城 / meiyuemall · **无直播**
