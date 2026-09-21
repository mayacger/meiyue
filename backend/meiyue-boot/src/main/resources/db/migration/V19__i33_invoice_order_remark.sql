-- I33：订单备注 / 发票快照 + 买家发票抬头

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS buyer_remark VARCHAR(256),
    ADD COLUMN IF NOT EXISTS invoice_title VARCHAR(128),
    ADD COLUMN IF NOT EXISTS invoice_tax_no VARCHAR(64),
    ADD COLUMN IF NOT EXISTS invoice_type VARCHAR(16);

COMMENT ON COLUMN orders.buyer_remark IS '买家下单备注（I33）';
COMMENT ON COLUMN orders.invoice_title IS '下单时发票抬头快照（I33 占位）';
COMMENT ON COLUMN orders.invoice_tax_no IS '税号快照';
COMMENT ON COLUMN orders.invoice_type IS 'PERSONAL / COMPANY；可空表示不开票';

CREATE TABLE buyer_invoice_profiles (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES user_accounts (id) ON DELETE CASCADE,
    title        VARCHAR(128) NOT NULL,
    tax_no       VARCHAR(64),
    invoice_type VARCHAR(16)  NOT NULL DEFAULT 'PERSONAL',
    is_default   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invoice_user ON buyer_invoice_profiles (user_id, is_default DESC, id DESC);

COMMENT ON TABLE buyer_invoice_profiles IS '买家发票抬头（I33 占位，无真实开票）';
