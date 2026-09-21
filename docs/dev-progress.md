# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I33 完成**（C19）  
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

- **I33**：发票抬头 CRUD；下单备注与发票快照；Flyway V19
- **I32**：未支付取消释放库存；签收后 N 天自动确认；搜索历史；Flyway V18
- **I31**：店铺运费 / 包邮门槛；结算运费展示
- **I30**：相关推荐；浏览足迹；评价审核

详见 [i32-capabilities.md](./i32-capabilities.md)、[i30-i31-capabilities.md](./i30-i31-capabilities.md)、[i33 见 i32 文档末节 / i33-capabilities.md](./i33-capabilities.md)。
