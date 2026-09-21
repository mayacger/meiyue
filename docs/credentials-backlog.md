# 待密钥联调清单（I38 收敛）

> 大功能暂停；以下仅列出需真实密钥/商户号后的联调项。  
> **禁止**把真实密钥提交入库；本地用 `.env` / 环境变量。

## 支付

| 项 | 变量 / 配置 | 状态 |
|----|-------------|------|
| 默认通道 | `MEIYUE_PAYMENT_DEFAULT_CHANNEL` | 现 `MOCK` |
| 微信 | `MEIYUE_WECHAT_*`（appId/mchId/apiKey/notifyUrl） | 占位未联调 |
| 支付宝 | `MEIYUE_ALIPAY_*` | 占位未联调 |
| 退款回调验签 | 支付模块 RefundGateway | MOCK 直过 |

## 安全 / 会话

| 项 | 变量 | 说明 |
|----|------|------|
| JWT | `MEIYUE_SECURITY_JWT_SECRET`（≥32） | 生产必换 |
| 登录验证码 | `MEIYUE_SECURITY_CAPTCHA_ENABLED` | 默认关；开启后需前端展示 |

## AI 素材

| 项 | 变量 | 说明 |
|----|------|------|
| Provider | `MEIYUE_AI_PROVIDER` | 现 `MOCK` |
| API Key | `MEIYUE_AI_API_KEY` | 空则不出真实图 |

## 物流面单

| 项 | 说明 |
|----|------|
| 电子面单 | 当前 MOCK ewaybill；真实承运商密钥未接 |

## 明确不做

- 直播 / 带货  
- 真实分账打款  
- 真实税务开票对接  

部署对照：[deploy.md](./deploy.md) · 备份：[backup-restore.md](./backup-restore.md)
