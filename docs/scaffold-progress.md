# 美月商城 · 脚手架 / 迭代进度说明（用户可读）

> 状态：**I1 → I9 已完成**（C19 持续开发）  
> 详细验证见 `docs/dev-progress.md` · PR #1

## 已完成

I1 入驻 · I2 商品装修 · I3 交易 · I4 支付 · I5 物流 · I6 售后 · I7 可观测 · I8 AI · **I9 Redis/前端/结算/店券**

## 构建

```bash
cd backend && mvn -q -DskipTests package
cd frontend && pnpm install && pnpm -r --filter "./apps/*" build
# 依赖：PostgreSQL + Redis（docker compose up -d）
```
