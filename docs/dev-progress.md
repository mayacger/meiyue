# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I31 完成**（C19）  
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

- **I31**：店铺默认运费 / 包邮门槛；结算运费预估；订单 goods/freight 拆分（无直播、无真实分账）
- **I30**：相关推荐；浏览足迹；Admin 评价隐藏/恢复；公开评价过滤；Flyway V17
- **I29**：退货物流填写体验；售后凭证图 URL；全站 Skeleton 骨架
- **I28**：平台 Banner CRUD + Buyer/Taro 展示；结算明细；Buyer SEO meta；Flyway V16

详见 [i30-i31-capabilities.md](./i30-i31-capabilities.md)、[i28-i29-capabilities.md](./i28-i29-capabilities.md)。
