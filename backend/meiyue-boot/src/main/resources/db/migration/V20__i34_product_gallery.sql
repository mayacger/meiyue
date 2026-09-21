-- I34：商品详情图集（JSON 数组 URL 列表）

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS gallery_image_urls TEXT;

COMMENT ON COLUMN products.gallery_image_urls IS '商品图集 URL JSON 数组，如 ["https://..."]（I34）';
