# 美月商城 · 买家 PC（web-buyer）

## 入口

- 包名：`@meiyue/web-buyer`
- 开发：`pnpm --filter @meiyue/web-buyer dev` → http://localhost:5173
- **非** Ant Design Pro；自研清透现代电商视觉

## 关系图

```text
SiteShell
  ├── Home（品牌英雄 + 商品带）
  ├── Products / ProductDetail
  ├── Cart → Checkout（券互斥）
  └── Orders / OrderDetail
        └── JWT → /api/v1/buyer/* · 公开 /api/v1/products
```

## 设计要点

墨绿主色、暖白底、品牌衬线仅用于「美月商城」、全宽英雄区、克制动效。
