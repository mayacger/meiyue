# meiyue-ai-assist（I8）

## 职责

AI 商品图 + AI 详情；可插拔 Provider；素材库与内容安全占位；挂接商家商品发布；失败降级人工上传。

**不做**：推广视频（二期）、直播/短视频带货。

## 入口

| HTTP | 说明 |
|------|------|
| `POST /api/v1/seller/ai/images` | 出图入库+审核 |
| `POST /api/v1/seller/ai/details` | 生成详情文案 |
| `POST /api/v1/seller/ai/products/{id}/cover` | 出图并挂封面 |
| `POST /api/v1/seller/ai/products/{id}/detail` | 生成并写入详情 |
| `POST /api/v1/seller/ai/manual-images` | 人工上传登记 `{"url":"..."}` |
| `GET /api/v1/seller/ai/assets` | 本店素材列表 |

配置：`meiyue.ai.provider=MOCK|OPENAI_COMPAT`；密钥 `MEIYUE_AI_API_KEY`（不入库）。

## 关系图

```
Seller ──► AiAssistController
              ├─ AiProviderRegistry ── MOCK / OPENAI_COMPAT
              ├─ ContentSafetyPort（占位）
              ├─ media_assets 入库
              └─ CatalogService.attachCover / applyDetailHtml
商品发布主路径仍可仅 POST /seller/products（人工 coverImageUrl），不依赖 AI。
```

## 降级

Provider 失败或审核拒绝 → `AI_DEGRADED` / `MEDIA_NOT_APPROVED`，响应提示人工上传；商品 CRUD 不受阻。
