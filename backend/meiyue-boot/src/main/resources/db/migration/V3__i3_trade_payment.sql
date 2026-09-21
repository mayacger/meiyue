-- I3：购物车 / 订单 / 支付（模拟）/ 库存扣减与超时释放
-- 订单状态：PENDING_PAYMENT → PAID | CANCELLED
-- 支付通道 MVP：MOCK（微信/支付宝占位枚举保留）

CREATE TABLE cart_items (
    id              BIGSERIAL PRIMARY KEY,
    buyer_user_id   BIGINT NOT NULL REFERENCES user_accounts (id),
    tenant_id       BIGINT NOT NULL REFERENCES tenants (id),
    sku_id          BIGINT NOT NULL REFERENCES product_skus (id),
    product_id      BIGINT NOT NULL REFERENCES products (id),
    quantity        INT NOT NULL CHECK (quantity > 0),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_cart_buyer_sku UNIQUE (buyer_user_id, sku_id)
);

CREATE INDEX idx_cart_buyer ON cart_items (buyer_user_id);

CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    order_no        VARCHAR(32) NOT NULL UNIQUE,
    buyer_user_id   BIGINT NOT NULL REFERENCES user_accounts (id),
    -- PENDING_PAYMENT | PAID | CANCELLED
    status          VARCHAR(32) NOT NULL,
    total_cents     BIGINT NOT NULL,
    -- 未支付超时时刻（创建时 = now + 30min）
    pay_expire_at   TIMESTAMPTZ NOT NULL,
    paid_at         TIMESTAMPTZ,
    cancelled_at    TIMESTAMPTZ,
    cancel_reason   VARCHAR(128),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_buyer ON orders (buyer_user_id);
CREATE INDEX idx_orders_status_expire ON orders (status, pay_expire_at);

CREATE TABLE order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    tenant_id       BIGINT NOT NULL REFERENCES tenants (id),
    product_id      BIGINT NOT NULL,
    sku_id          BIGINT NOT NULL,
    product_title   VARCHAR(256) NOT NULL,
    sku_code        VARCHAR(64) NOT NULL,
    spec_text       VARCHAR(256),
    unit_price_cents BIGINT NOT NULL,
    quantity        INT NOT NULL,
    line_total_cents BIGINT NOT NULL
);

CREATE INDEX idx_order_items_order ON order_items (order_id);
CREATE INDEX idx_order_items_tenant ON order_items (tenant_id);

CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    payment_no      VARCHAR(32) NOT NULL UNIQUE,
    order_id        BIGINT NOT NULL REFERENCES orders (id),
    -- MOCK | WECHAT | ALIPAY
    channel         VARCHAR(32) NOT NULL,
    -- PENDING | SUCCESS | FAILED | CLOSED
    status          VARCHAR(32) NOT NULL,
    amount_cents    BIGINT NOT NULL,
    -- 通道侧流水（模拟支付写入 mock_xxx）
    channel_trade_no VARCHAR(64),
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_order ON payments (order_id);
