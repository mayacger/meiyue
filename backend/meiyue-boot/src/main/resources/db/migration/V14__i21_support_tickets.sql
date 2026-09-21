-- I21：客服工单（MVP，非 IM）
-- 买家开单 / 商家或平台回复 / 关闭

CREATE TABLE support_tickets (
    id                  BIGSERIAL PRIMARY KEY,
    ticket_no           VARCHAR(32)  NOT NULL UNIQUE,
    user_id             BIGINT       NOT NULL REFERENCES user_accounts (id),
    tenant_id           BIGINT,
    subject             VARCHAR(128) NOT NULL,
    body                VARCHAR(2000) NOT NULL,
    category            VARCHAR(32)  NOT NULL DEFAULT 'GENERAL',
    status              VARCHAR(32)  NOT NULL DEFAULT 'OPEN',
    seller_reply        VARCHAR(2000),
    admin_reply         VARCHAR(2000),
    replied_at          TIMESTAMPTZ,
    closed_at           TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_support_tickets_user ON support_tickets (user_id, created_at DESC);
CREATE INDEX idx_support_tickets_tenant_status ON support_tickets (tenant_id, status);
CREATE INDEX idx_support_tickets_status ON support_tickets (status, created_at DESC);

COMMENT ON TABLE support_tickets IS '客服工单 MVP：留言式非 IM；tenant_id 可空表示平台工单';
COMMENT ON COLUMN support_tickets.category IS 'GENERAL / ORDER / AFTERSALE / PRODUCT';
COMMENT ON COLUMN support_tickets.status IS 'OPEN / REPLIED / CLOSED';
