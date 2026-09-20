-- I4：支付安全基线 + 周期结算账本（MVP 记账；官方分账二期前再接）
-- 密钥不入库：仅配置/环境变量

-- 通道回调幂等落库：channel + channel_trade_no 唯一
CREATE TABLE payment_notify_logs (
    id                  BIGSERIAL PRIMARY KEY,
    channel             VARCHAR(32)  NOT NULL,
    -- 通道侧流水号（幂等键）
    channel_trade_no    VARCHAR(128) NOT NULL,
    payment_no          VARCHAR(32),
    order_id            BIGINT,
    -- 验签结果：PASS / FAIL / SKIP_MOCK
    verify_result       VARCHAR(32)  NOT NULL,
    -- 处理结果：IGNORED / SUCCESS / DUPLICATE / ERROR
    process_result      VARCHAR(32)  NOT NULL,
    -- 原始通知摘要（勿存完整密钥材料；可存脱敏 body hash）
    payload_hash        VARCHAR(128),
    raw_body_preview    VARCHAR(1024),
    error_message       VARCHAR(512),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_notify_channel_trade UNIQUE (channel, channel_trade_no)
);

CREATE INDEX idx_notify_payment_no ON payment_notify_logs (payment_no);

COMMENT ON TABLE payment_notify_logs IS '支付回调验签与幂等日志';

-- 主动查单记录
CREATE TABLE payment_query_logs (
    id                  BIGSERIAL PRIMARY KEY,
    payment_no          VARCHAR(32)  NOT NULL,
    channel             VARCHAR(32)  NOT NULL,
    -- SUCCESS / PENDING / NOT_FOUND / ERROR
    query_result        VARCHAR(32)  NOT NULL,
    channel_trade_no    VARCHAR(128),
    raw_preview         VARCHAR(1024),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_query_payment_no ON payment_query_logs (payment_no);

COMMENT ON TABLE payment_query_logs IS '支付主动查单日志（补偿）';

-- 日对账批次
CREATE TABLE payment_reconcile_batches (
    id                  BIGSERIAL PRIMARY KEY,
    -- 对账日（业务日，Asia/Shanghai）
    biz_date            DATE         NOT NULL,
    channel             VARCHAR(32)  NOT NULL,
    -- PENDING / RUNNING / DONE / FAILED
    status              VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    local_count         INT          NOT NULL DEFAULT 0,
    channel_count       INT          NOT NULL DEFAULT 0,
    diff_count          INT          NOT NULL DEFAULT 0,
    note                VARCHAR(512),
    started_at          TIMESTAMPTZ,
    finished_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_reconcile_day_channel UNIQUE (biz_date, channel)
);

COMMENT ON TABLE payment_reconcile_batches IS '日对账批次占位';

CREATE TABLE payment_reconcile_diffs (
    id                  BIGSERIAL PRIMARY KEY,
    batch_id            BIGINT       NOT NULL REFERENCES payment_reconcile_batches (id) ON DELETE CASCADE,
    -- LOCAL_ONLY / CHANNEL_ONLY / AMOUNT_MISMATCH
    diff_type           VARCHAR(32)  NOT NULL,
    payment_no          VARCHAR(32),
    channel_trade_no    VARCHAR(128),
    local_amount_cents  BIGINT,
    channel_amount_cents BIGINT,
    detail              VARCHAR(512),
    -- OPEN / RESOLVED
    status              VARCHAR(32)  NOT NULL DEFAULT 'OPEN',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reconcile_diff_batch ON payment_reconcile_diffs (batch_id);

COMMENT ON TABLE payment_reconcile_diffs IS '日对账差异（进工单前占位）';

-- 周期结算账本（MVP 平台代收 + 记账；打款可半人工）
CREATE TABLE settlement_ledgers (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL REFERENCES tenants (id),
    -- 关联订单行或订单
    order_id            BIGINT       NOT NULL,
    order_item_id       BIGINT,
    -- SALE / REFUND / ADJUST
    entry_type          VARCHAR(32)  NOT NULL,
    -- 金额：分；卖家应得为正，扣减为负
    amount_cents        BIGINT       NOT NULL,
    -- PENDING / SETTLED / VOID
    status              VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    -- 账期标识，如 2026-09-W3
    period_key          VARCHAR(32)  NOT NULL,
    remark              VARCHAR(256),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    settled_at          TIMESTAMPTZ
);

CREATE INDEX idx_settlement_tenant ON settlement_ledgers (tenant_id, status);
CREATE INDEX idx_settlement_order ON settlement_ledgers (order_id);

COMMENT ON TABLE settlement_ledgers IS '商家周期结算账本（MVP 记账；官方分账二期前再接）';

-- payments 表补充：通知次数 / 最近查单时间（若列已存在则跳过由 Flyway 版本保证首次）
ALTER TABLE payments ADD COLUMN IF NOT EXISTS notify_count INT NOT NULL DEFAULT 0;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS last_query_at TIMESTAMPTZ;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS idempotent_key VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS uk_payments_idempotent_key
    ON payments (idempotent_key) WHERE idempotent_key IS NOT NULL;
