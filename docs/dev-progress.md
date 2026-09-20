# 美月商城 · 开发进度（用户可读）

> 状态：**I1 → I11 已完成**（持续开发授权 C19）  
> 仓库：`mayacger/meiyue` · 分支：`cursor/meiyue-mall-scaffold-9727` · PR #1  
> 规划：`ecommerce-platform-plan.md` / `project-context.md` **v1.2 + C19**  
> 详档：`docs/i10-phase2-capabilities.md` · `docs/i11-capabilities.md`

---

## 入口

| 入口 | 说明 |
|------|------|
| API | `backend/meiyue-boot` → `:8080` |
| 商家端 | `frontend/apps/web-seller` → `:5174` |
| 平台端 | `frontend/apps/web-admin` → `:5175` |
| 买家端 | `frontend/apps/web-buyer` → `:5173` |
| Compose | `docker/docker-compose.yml`（PostgreSQL + Redis） |

种子：`admin`/`admin123`；联调 `seller1`/`seller123`、`buyer1`/`buyer123`

---

## I1–I10（摘要）

- I1–I8 入驻到 AI 图/详情 · I9 Redis/店券/结算 · I10 平台券/评价/搜索/装修楼层/通知骨架  
- 详阅历史章节与模块 README

---

## I11 已交付

### 通知事件自动投递

- `NotificationPublisher` + `meiyue.notify.events-enabled`
- 挂钩：下单、支付、发货/送达、确认收货、售后申请/审结

### 装修可视化

- Seller 楼层列表增删改排序 + 主题色；非自由画布；禁直播

### 电子面单 + 多包裹

- Flyway V11：`package_seq` / `ewaybill_*`
- MOCK 打单；一单多运单 + 按行拆包

### AI 推广视频 MOCK

- `ai_video_tasks` + Redis 消费 → `media_assets(VIDEO)` → `products.promo_video_*`
- **非直播**；真实模型后置

### I11 验证

```bash
cd backend && mvn -q -DskipTests package
# 通知开关
# meiyue.notify.events-enabled=true
# 发货 MOCK 面单
POST /api/v1/seller/shipments  {"orderId":1,"carrierCode":"SF","printEwaybill":true}
# 推广视频
POST /api/v1/seller/ai/videos  {"prompt":"商品展示","productId":1}
# 装修可视化：seller /decoration
```

---

## 下一步

- 真实微信/支付宝 / AI 外呼与视频模型联调
- 真实电子面单通道；通知推送通道
- 官方分账打款（需商户资质，本迭代仅账本）

---

## 已知限制

- 跨店订单暂不支持店券；平台券与店券默认互斥
- 面单 / 推广视频均为 MOCK 占位
- 售后退款与结算仍为账本记账；无官方分账打款
- 站内通知无 App/短信推送
