# Taro 买家端体积与分包说明（I37）

> 目标：微信主包可控；无直播资源。  
> 入口：`frontend/apps/taro-buyer`

## 现状

- Tab 主包页：`index` / `category` / `cart` / `mine`
- 二级页同仓：`detail`、`orders`、`address`、收藏/足迹等（当前仍在主包 pages 列表）
- 构建产物参考：`dist/weapp/taro.js` ≈ 210KB（gzip ~69KB）；业务页各自拆 chunk

## 已启用

`config/index.ts` → `mini.optimizeMainPackage.enable = true`  
Taro 会在编译期尽量把非入口依赖下沉，减轻主包。

## 推荐分包（后续可落）

```text
主包 pages/
  index · category · cart · mine
分包 package-trade/
  detail · orders · order-detail · address · settings
分包 package-user/
  favorites · browse-history · coupons · notifications · store
```

迁移步骤：移动目录 → `app.config.ts` 增加 `subPackages` → 所有 `navigateTo` 路径同步。  
本轮不搬迁文件，避免打断现有 smoke；文档先行。

## 验证

```bash
pnpm --filter @meiyue/taro-buyer build:weapp
# 用微信开发者工具查看「代码依赖分析」主包体积
```
