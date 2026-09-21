# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I29 完成**（C19）  
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

- **I29**：退货物流填写体验；售后凭证图 URL；全站 Skeleton 骨架
- **I28**：平台 Banner CRUD + Buyer/Taro 展示；结算明细；Buyer SEO meta；Flyway V16
- **I27**：看板图表；商品草稿箱
- **I26**：员工管理；OpenAPI（demo/dev）

详见 [i28-i29-capabilities.md](./i28-i29-capabilities.md)、[i26-i27-capabilities.md](./i26-i27-capabilities.md)。
