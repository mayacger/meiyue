-- I22：店铺资料扩展 + 买家商品收藏
-- I23：平台运营配置（费率只读种子）

ALTER TABLE stores
    ADD COLUMN IF NOT EXISTS description VARCHAR(512),
    ADD COLUMN IF NOT EXISTS logo_url VARCHAR(1024);

COMMENT ON COLUMN stores.description IS '店铺简介；买家店页展示';
COMMENT ON COLUMN stores.logo_url IS '店铺 Logo URL（占位，非强制 OSS）';

CREATE TABLE product_favorites (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES user_accounts (id) ON DELETE CASCADE,
    product_id      BIGINT       NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_favorite_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX idx_favorites_user ON product_favorites (user_id, created_at DESC);

COMMENT ON TABLE product_favorites IS '买家商品收藏';

CREATE TABLE platform_configs (
    config_key      VARCHAR(64) PRIMARY KEY,
    config_value    VARCHAR(256) NOT NULL,
    description     VARCHAR(256),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE platform_configs IS '平台运营配置；费率只读展示，无真实分账打款';

INSERT INTO platform_configs (config_key, config_value, description) VALUES
    ('platform_fee_rate_bps', '50', '平台服务费率（基点，50=0.5%）；只读展示'),
    ('settlement_cycle', 'WEEKLY', '结算周期：WEEKLY / MONTHLY；只读展示'),
    ('min_withdraw_cents', '10000', '最低提现门槛（分）；只读展示')
ON CONFLICT (config_key) DO NOTHING;
