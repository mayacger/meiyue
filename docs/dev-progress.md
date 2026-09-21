# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I27 完成**（C19）  
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

- **I27**：Seller/Admin 概览近 7 日图表（Recharts）；商品草稿箱 `GET /seller/products/drafts`
- **I26**：商家员工邀请/列表/移除；`SELLER_STAFF` 权限收紧；springdoc OpenAPI（仅 demo/dev）
- **I25**：消息已读/未读；批量上下架；库存预警
- **I24**：改密/资料；审计日志；物流时间线

详见 [i26-i27-capabilities.md](./i26-i27-capabilities.md)、[i24-i25-capabilities.md](./i24-i25-capabilities.md)。
