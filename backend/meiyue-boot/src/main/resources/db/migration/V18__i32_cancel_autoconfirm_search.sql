-- I32：自动确认收货字段 + 搜索历史 + 自动确认天数配置
-- I33 字段见 V19

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS delivered_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS auto_confirm_at TIMESTAMPTZ;

COMMENT ON COLUMN orders.delivered_at IS '全部正向包裹签收时间（I32）';
COMMENT ON COLUMN orders.auto_confirm_at IS '计划自动确认收货时间；到点由定时任务完成';

CREATE INDEX IF NOT EXISTS idx_orders_auto_confirm
    ON orders (status, auto_confirm_at)
    WHERE auto_confirm_at IS NOT NULL;

CREATE TABLE buyer_search_histories (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES user_accounts (id) ON DELETE CASCADE,
    keyword      VARCHAR(64)  NOT NULL,
    searched_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_search_user_keyword UNIQUE (user_id, keyword)
);

CREATE INDEX idx_search_user_time ON buyer_search_histories (user_id, searched_at DESC);

COMMENT ON TABLE buyer_search_histories IS '买家搜索关键词历史（I32）；同词 upsert';

INSERT INTO platform_configs (config_key, config_value, description) VALUES
    ('auto_confirm_receipt_days', '7', '签收后自动确认收货天数（I32）；0=签收后立即确认')
ON CONFLICT (config_key) DO NOTHING;
