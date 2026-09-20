# meiyue-decoration（I2）

入口：`DecorationController`  
- 模板：`GET /api/v1/decoration/templates`  
- 商家草稿/发布：`/api/v1/seller/decoration/**`  
- 买家只读：`GET /api/v1/stores/{tenantId}/page`

楼层类型：BANNER / PRODUCT_RECOMMEND / IMAGE_TEXT / PRODUCT_GROUP  
**禁止直播组件**（服务端校验 floorsJson）。
