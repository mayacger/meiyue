-- I36：商品软删（回收站）

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_products_deleted_at ON products (deleted_at);

COMMENT ON COLUMN products.deleted_at IS '软删时间；非空表示在回收站，默认列表排除（I36）';
