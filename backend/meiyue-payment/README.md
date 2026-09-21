# 美月商城 · 支付模块（I3/I4）
#
# 入口
#   PaymentNotifyController  /api/v1/payments/notify/{mock|wechat|alipay}
#   PaymentService           建单 / 回调幂等 / 查单
#   SettlementLedgerService  周期结算账本
#   PaymentQueryJob / PaymentReconcileJob
#
# 关系
#   Order --创建--> Payment --回调/查单--> SUCCESS
#                              └─► PaymentSuccessHandler → 订单PAID + 结算账本
#
# 安全
#   - 验签在 ChannelClient；金额以后端支付单为准
#   - 幂等键：channel + channel_trade_no（notify_logs 唯一约束）
#   - 密钥：meiyue.payment.* / 环境变量，不入库
#
# 本地：default-channel=MOCK，mock-enabled=true
