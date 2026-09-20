-- I12：支付退款幂等日志（通道退款占位；非官方分账打款）

CREATE TABLE payment_refunds (
    id                  BIGSERIAL PRIMARY KEY,
    payment_no          VARCHAR(32)  NOT NULL,
    order_id            BIGINT       NOT NULL,
    -- 售后单唯一：同一售后只退一次（幂等键）
    aftersale_id        BIGINT       NOT NULL,
    amount_cents        BIGINT       NOT NULL,
    channel             VARCHAR(32)  NOT NULL,
    -- 通道退款流水（MOCK 为 mock_rf_...）
    channel_refund_no   VARCHAR(128),
    -- SUCCESS | FAILED
    status              VARCHAR(16)  NOT NULL,
    message             VARCHAR(512),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_payment_refund_aftersale UNIQUE (aftersale_id)
);

CREATE INDEX idx_payment_refunds_payment ON payment_refunds (payment_no);
CREATE INDEX idx_payment_refunds_order ON payment_refunds (order_id);

COMMENT ON TABLE payment_refunds IS 'I12 通道退款记录；按 aftersale_id 幂等；不做官方分账打款';
