-- I30：浏览足迹 + 评价隐藏
-- I31：店铺运费模板 + 订单运费字段
-- 无直播、无真实分账

CREATE TABLE product_browse_histories (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES user_accounts (id) ON DELETE CASCADE,
    product_id      BIGINT       NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    browsed_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_browse_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX idx_browse_user_time ON product_browse_histories (user_id, browsed_at DESC);

COMMENT ON TABLE product_browse_histories IS '买家浏览足迹（I30）；同一商品只保留最近一次';

ALTER TABLE product_reviews
    ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS hidden_reason VARCHAR(256),
    ADD COLUMN IF NOT EXISTS hidden_at TIMESTAMPTZ;

COMMENT ON COLUMN product_reviews.hidden IS '平台审核隐藏（I30）；公开列表过滤';

-- I31：店铺默认运费（分）与包邮门槛（分，NULL=不包邮）
ALTER TABLE stores
    ADD COLUMN IF NOT EXISTS freight_cents BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS free_shipping_threshold_cents BIGINT;

COMMENT ON COLUMN stores.freight_cents IS '默认运费（分）；0 表示免运费';
COMMENT ON COLUMN stores.free_shipping_threshold_cents IS '包邮门槛（分）；NULL 表示无包邮';

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS goods_cents BIGINT,
    ADD COLUMN IF NOT EXISTS freight_cents BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN orders.goods_cents IS '商品应付（券后、运费前，分）';
COMMENT ON COLUMN orders.freight_cents IS '运费合计（分）；多店累加';

-- 历史订单：goods_cents 回填为 total_cents，运费 0
UPDATE orders SET goods_cents = total_cents WHERE goods_cents IS NULL;
