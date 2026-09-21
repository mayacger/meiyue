# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I19 完成**（C19+C20）  
> 分支：`cursor/meiyue-mall-scaffold-9727` · PR #1  

## 验证

```bash
cd backend && mvn -q -pl meiyue-identity,meiyue-catalog -am -Dsurefire.failIfNoSpecifiedTests=false -Dtest=AdminUserServiceTest,CategoryUpsertRequestTest test
cd frontend && pnpm install && pnpm build
pnpm build:taro:h5 && pnpm --filter @meiyue/taro-buyer build:weapp
```

## 最近交付

- **I19**：搜索类目筛选、评价展示/提交、通知箱四端缺页补齐（Buyer PC / Taro / Seller）
- **I18**：平台类目写接口 + Admin 可管理；买家地址簿 CRUD + PC/Taro；Admin 用户目录；Flyway V13
- I16 四端完整化 · I17 售后列表与空态  
