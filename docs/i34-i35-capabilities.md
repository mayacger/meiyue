# I34 / I35 能力说明

> 同一 PR · Flyway **V20** · 无直播、无真实分账  

## 关系图

```
I34 生产部署
  backend/Dockerfile ──► meiyue-api jar
  frontend/Dockerfile.buyer + nginx.conf ──► 静态 SPA + /api 反代
  docker-compose.prod.yml ──► api + web-buyer（依赖 postgres/redis）
  docs/deploy.md · .env.prod.example（密钥不入库）

I34 商品多媒体
  products.gallery_image_urls (JSON TEXT)
       └── Seller 新建/编辑（每行 URL）
              └── Buyer PC / Taro 详情 gallery + promoVideoUrl 播放

I35 安全占位
  MEIYUE_SECURITY_CAPTCHA_ENABLED
       └── GET /auth/captcha → 登录页可选展示
  改密 / 导出 CSV → 前端二次确认（Modal / confirm / showModal）
```

## 入口

| 能力 | 入口 |
|------|------|
| 部署 | `docs/deploy.md` · `docker compose … docker-compose.prod.yml` |
| 图集编辑 | Seller `/products` 新建/编辑 Modal |
| 图集/视频展示 | Buyer `/products/:id` · Taro `pages/detail` |
| 验证码 | `GET /api/v1/auth/captcha` · 四端登录 |
| 二次确认 | Buyer/Seller/Admin 改密；Seller 发货页 / Admin 配置页导出 |

## 冒烟

`smoke-e2e.sh` / `smoke-i18.sh` 覆盖图集写入回读与 captcha `enabled=false`。
