# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I25 完成**（C19+C20）  
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

- **I25**：消息中心已读/未读筛选；商品批量上下架；库存预警列表 `/seller/inventory/alerts`
- **I24**：改密/个人资料（买家·商家·平台）；Admin 审计日志 ProTable；物流轨迹时间线（PC/Taro/Seller）
- **I23**：领券中心；平台费率只读；EmptyState/ErrorState
- **I22**：店铺设置、收藏、素材选用、订单 CSV；Flyway V15

详见 [i22-i23-capabilities.md](./i22-i23-capabilities.md)、[i24-i25-capabilities.md](./i24-i25-capabilities.md)。
