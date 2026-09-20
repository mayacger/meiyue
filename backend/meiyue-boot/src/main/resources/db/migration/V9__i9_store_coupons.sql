-- I9：店券最小闭环（商家发券 / 买家领券 / 下单抵扣）
-- 平台券后置；不做直播相关

CREATE TABLE store_coupons (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL REFERENCES tenants (id),
    -- 券码（店内唯一）
    code                VARCHAR(32)  NOT NULL,
    title               VARCHAR(128) NOT NULL,
    -- 面额（分）
    discount_cents      BIGINT       NOT NULL,
    -- 最低消费（分），0 表示无门槛
    min_spend_cents     BIGINT       NOT NULL DEFAULT 0,
    -- 发行总量；0 表示不限
    total_quota         INT          NOT NULL DEFAULT 0,
    claimed_count       INT          NOT NULL DEFAULT 0,
    -- ACTIVE | DISABLED | EXPIRED
    status              VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    starts_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    ends_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, code)
);

CREATE INDEX idx_store_coupons_tenant ON store_coupons (tenant_id, status);

CREATE TABLE coupon_claims (
    id                  BIGSERIAL PRIMARY KEY,
    coupon_id           BIGINT       NOT NULL REFERENCES store_coupons (id),
    tenant_id           BIGINT       NOT NULL REFERENCES tenants (id),
    buyer_user_id       BIGINT       NOT NULL REFERENCES user_accounts (id),
    -- CLAIMED | USED | VOID
    status              VARCHAR(16)  NOT NULL DEFAULT 'CLAIMED',
    -- 使用时绑定订单
    order_id            BIGINT,
    discount_applied_cents BIGINT,
    claimed_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    used_at             TIMESTAMPTZ,
    UNIQUE (coupon_id, buyer_user_id)
);

CREATE INDEX idx_coupon_claims_buyer ON coupon_claims (buyer_user_id, status);

COMMENT ON TABLE store_coupons IS '店券模板；平台券后置';
COMMENT ON TABLE coupon_claims IS '买家领券记录；下单抵扣后标记 USED';
