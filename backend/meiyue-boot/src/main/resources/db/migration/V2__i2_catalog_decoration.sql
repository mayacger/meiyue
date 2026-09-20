-- I2：类目 / 商品 SPU·SKU / 店铺装修（模板楼层）
-- 硬约束：无直播楼层组件

CREATE TABLE categories (
    id              BIGSERIAL PRIMARY KEY,
    parent_id       BIGINT REFERENCES categories (id),
    name            VARCHAR(128) NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0,
    -- ENABLED / DISABLED
    status          VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE categories IS '平台统一类目';

CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES tenants (id),
    category_id     BIGINT REFERENCES categories (id),
    title           VARCHAR(256) NOT NULL,
    subtitle        VARCHAR(512),
    detail_html     TEXT,
    -- DRAFT / ON_SALE / OFF_SALE
    status          VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_tenant ON products (tenant_id);
CREATE INDEX idx_products_status ON products (tenant_id, status);

COMMENT ON TABLE products IS '商品 SPU；行级 tenant_id 隔离';

CREATE TABLE product_skus (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES tenants (id),
    product_id      BIGINT NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    sku_code        VARCHAR(64) NOT NULL,
    spec_text       VARCHAR(256),
    -- 价格：分
    price_cents     BIGINT NOT NULL,
    -- 可售库存（I3 再做预占）
    stock_qty       INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_sku_code_tenant UNIQUE (tenant_id, sku_code)
);

CREATE INDEX idx_skus_product ON product_skus (product_id);

COMMENT ON TABLE product_skus IS '商品 SKU';

-- 装修模板（平台预置）
CREATE TABLE decoration_templates (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(64) NOT NULL UNIQUE,
    name            VARCHAR(128) NOT NULL,
    description     VARCHAR(512),
    -- 默认楼层 JSON 数组
    default_floors_json TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE decoration_templates IS '店铺装修模板（非自由拖拽）';

-- 店铺页面：草稿 / 已发布
CREATE TABLE store_pages (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES tenants (id),
    store_id        BIGINT NOT NULL REFERENCES stores (id),
    template_code   VARCHAR(64) NOT NULL,
    -- 主题色，如 #1a5f4a
    theme_color     VARCHAR(32) NOT NULL DEFAULT '#1a5f4a',
    -- 楼层配置 JSON
    floors_json     TEXT NOT NULL,
    -- DRAFT / PUBLISHED
    status          VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    published_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_store_page_tenant_status UNIQUE (tenant_id, status)
);

-- 注意：UNIQUE(tenant_id, status) 意味着每个租户最多 1 份 DRAFT + 1 份 PUBLISHED
COMMENT ON TABLE store_pages IS '店铺装修页；买家只读 PUBLISHED';

-- 预置 3 套模板（无直播楼层）
INSERT INTO decoration_templates (code, name, description, default_floors_json) VALUES
('simple_banner', '简约 Banner', 'Banner + 商品推荐',
 '[{"type":"BANNER","enabled":true,"title":"欢迎光临","imageUrl":""},{"type":"PRODUCT_RECOMMEND","enabled":true,"title":"热卖推荐","productIds":[]}]'),
('image_text', '图文故事', '图文楼层 + 商品分组',
 '[{"type":"IMAGE_TEXT","enabled":true,"title":"品牌故事","body":"讲述你的品牌","imageUrl":""},{"type":"PRODUCT_GROUP","enabled":true,"title":"精选分组","productIds":[]}]'),
('full_showcase', '完整橱窗', 'Banner + 图文 + 推荐 + 分组',
 '[{"type":"BANNER","enabled":true,"title":"季节上新","imageUrl":""},{"type":"IMAGE_TEXT","enabled":true,"title":"本季主打","body":"","imageUrl":""},{"type":"PRODUCT_RECOMMEND","enabled":true,"title":"人气好物","productIds":[]},{"type":"PRODUCT_GROUP","enabled":true,"title":"全部商品","productIds":[]}]');

-- 示例类目
INSERT INTO categories (name, sort_order) VALUES
('鲜花绿植', 10),
('家居日用', 20),
('美妆个护', 30);
