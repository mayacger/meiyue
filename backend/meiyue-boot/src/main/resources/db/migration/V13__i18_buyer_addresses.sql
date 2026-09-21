-- I18：买家地址簿
-- 平台级账号绑定；无 tenant_id（买家跨店）

CREATE TABLE buyer_addresses (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT       NOT NULL REFERENCES user_accounts (id) ON DELETE CASCADE,
    receiver_name       VARCHAR(64)  NOT NULL,
    receiver_phone      VARCHAR(32)  NOT NULL,
    province            VARCHAR(64)  NOT NULL,
    city                VARCHAR(64)  NOT NULL,
    district            VARCHAR(64)  NOT NULL,
    detail_address      VARCHAR(256) NOT NULL,
    is_default          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_buyer_addresses_user ON buyer_addresses (user_id);

COMMENT ON TABLE buyer_addresses IS '买家收货地址簿；平台级 user_id 绑定';
COMMENT ON COLUMN buyer_addresses.is_default IS '默认地址；同一用户至多一个 true（业务层保证）';
