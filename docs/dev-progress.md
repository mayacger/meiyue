# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I38 完成**（C19；I38 为收敛清单）  
> 分支：`cursor/meiyue-mall-scaffold-9727` · PR #1  

## 验证

```bash
cd backend && mvn -q -DskipTests package
cd frontend && pnpm install && pnpm build
pnpm build:taro:h5 && pnpm --filter @meiyue/taro-buyer build:weapp
# 可选：BASE_URL=http://localhost:8080 ./scripts/smoke-e2e.sh
# 可选：BASE_URL=http://localhost:8080 ./scripts/smoke-i18.sh
```

## 最近交付

- **I38**：`docs/credentials-backlog.md` 待密钥联调清单；暂停大功能
- **I37**：Buyer 焦点/aria、页脚关于/帮助；Taro 主包优化说明
- **I36**：商品软删/回收站（Seller+Admin）；销售日/周 CSV；Flyway V21
- **I35**：登录验证码开关；改密/导出二次确认
- **I34**：生产镜像/compose；商品图集与推广视频

详见 [i36-i38-capabilities.md](./i36-i38-capabilities.md)、[credentials-backlog.md](./credentials-backlog.md)、[taro-bundle.md](./taro-bundle.md)、[i34-i35-capabilities.md](./i34-i35-capabilities.md)、[deploy.md](./deploy.md)。
