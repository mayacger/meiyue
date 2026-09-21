# meiyue-catalog（I2）

入口：`CatalogController`  
- 公开：`GET /api/v1/categories|products|products/{id}|stores/{tenantId}/products`  
- 商家：`/api/v1/seller/products/**`

关系：Category(平台) ← Product(SPU, tenant_id) ← ProductSku  
防越权：写操作仅用 JWT 注入的 tenantId。
