# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I21 完成**（C19+C20）  
> 分支：`cursor/meiyue-mall-scaffold-9727` · PR #1  

## 验证

```bash
cd backend && mvn -q -DskipTests package
cd frontend && pnpm install && pnpm build
pnpm build:taro:h5 && pnpm --filter @meiyue/taro-buyer build:weapp
# 可选：BASE_URL=http://localhost:8080 ./scripts/smoke-i18.sh
```

## 最近交付

- **I21**：客服工单 MVP（买家/商家/平台，非 IM）；商家评价待回复筛选与回复体验  
- **I20**：Seller 经营概览 + 库存管理；Admin 概览对齐 `/admin/dashboard`；Buyer PC 首页/详情打磨；`scripts/smoke-i18.sh`  
- I18/I19：类目/地址/用户 · 搜索/评价/通知  
