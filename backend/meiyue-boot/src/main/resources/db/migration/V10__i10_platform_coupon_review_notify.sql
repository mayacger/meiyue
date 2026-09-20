-- I10：平台券、商品评价、站内通知骨架
-- 装修增强：更多楼层类型默认模板（仍禁止直播）

-- ========== 平台券 ==========
CREATE TABLE platform_coupons (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(32)  NOT NULL UNIQUE,
    title               VARCHAR(128) NOT NULL,
    discount_cents      BIGINT       NOT NULL,
    min_spend_cents     BIGINT       NOT NULL DEFAULT 0,
    total_quota         INT          NOT NULL DEFAULT 0,
    claimed_count       INT          NOT NULL DEFAULT 0,
    status              VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    starts_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    ends_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE platform_coupon_claims (
    id                  BIGSERIAL PRIMARY KEY,
    coupon_id           BIGINT       NOT NULL REFERENCES platform_coupons (id),
    buyer_user_id       BIGINT       NOT NULL REFERENCES user_accounts (id),
    status              VARCHAR(16)  NOT NULL DEFAULT 'CLAIMED',
    order_id            BIGINT,
    discount_applied_cents BIGINT,
    claimed_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    used_at             TIMESTAMPTZ,
    UNIQUE (coupon_id, buyer_user_id)
);

CREATE INDEX idx_platform_claims_buyer ON platform_coupon_claims (buyer_user_id, status);

COMMENT ON TABLE platform_coupons IS '平台券；与店券默认互斥（见业务规则）';

-- ========== 商品评价 ==========
CREATE TABLE product_reviews (
    id                  BIGSERIAL PRIMARY KEY,
    product_id          BIGINT       NOT NULL REFERENCES products (id),
    order_id            BIGINT       NOT NULL REFERENCES orders (id),
    order_item_id       BIGINT       NOT NULL,
    tenant_id           BIGINT       NOT NULL REFERENCES tenants (id),
    buyer_user_id       BIGINT       NOT NULL REFERENCES user_accounts (id),
    -- 1..5
    rating              INT          NOT NULL,
    content             VARCHAR(1000) NOT NULL,
    -- 商家回复（可空）
    seller_reply        VARCHAR(1000),
    replied_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (order_item_id)
);

CREATE INDEX idx_reviews_product ON product_reviews (product_id, created_at DESC);
CREATE INDEX idx_reviews_tenant ON product_reviews (tenant_id, created_at DESC);

COMMENT ON TABLE product_reviews IS '确认收货(COMPLETED)后评价；一行订单行一条';

-- ========== 站内通知骨架 ==========
CREATE TABLE notifications (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT       NOT NULL REFERENCES user_accounts (id),
    -- BUYER | SELLER | PLATFORM
    audience            VARCHAR(16)  NOT NULL,
    title               VARCHAR(128) NOT NULL,
    body                VARCHAR(512) NOT NULL,
    -- ORDER | AFTERSALE | COUPON | SYSTEM | REVIEW
    category            VARCHAR(32)  NOT NULL DEFAULT 'SYSTEM',
    ref_type            VARCHAR(32),
    ref_id              VARCHAR(64),
    read_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user ON notifications (user_id, created_at DESC);

COMMENT ON TABLE notifications IS '站内信骨架；非推送通道';

-- ========== 装修模板增强：更多楼层类型 + sortOrder ==========
UPDATE decoration_templates SET default_floors_json =
 '[{"type":"BANNER","sortOrder":10,"enabled":true,"title":"欢迎光临","imageUrl":""},{"type":"CATEGORY_NAV","sortOrder":20,"enabled":true,"title":"类目导航","categoryIds":[]},{"type":"PRODUCT_RECOMMEND","sortOrder":30,"enabled":true,"title":"热卖推荐","productIds":[]},{"type":"COUPON_ENTRY","sortOrder":40,"enabled":true,"title":"领券中心"}]'
WHERE code = 'simple_banner';

UPDATE decoration_templates SET default_floors_json =
 '[{"type":"IMAGE_TEXT","sortOrder":10,"enabled":true,"title":"品牌故事","body":"讲述你的品牌","imageUrl":""},{"type":"IMAGE_STRIP","sortOrder":20,"enabled":true,"title":"图集","imageUrls":[]},{"type":"PRODUCT_GROUP","sortOrder":30,"enabled":true,"title":"精选分组","productIds":[]},{"type":"RICH_TEXT","sortOrder":40,"enabled":true,"title":"店铺公告","html":"<p>欢迎选购</p>"}]'
WHERE code = 'image_text';

UPDATE decoration_templates SET default_floors_json =
 '[{"type":"BANNER","sortOrder":10,"enabled":true,"title":"季节上新","imageUrl":""},{"type":"CATEGORY_NAV","sortOrder":15,"enabled":true,"title":"逛类目","categoryIds":[]},{"type":"IMAGE_TEXT","sortOrder":20,"enabled":true,"title":"本季主打","body":"","imageUrl":""},{"type":"PRODUCT_RECOMMEND","sortOrder":30,"enabled":true,"title":"人气好物","productIds":[]},{"type":"COUPON_ENTRY","sortOrder":35,"enabled":true,"title":"优惠券"},{"type":"PRODUCT_GROUP","sortOrder":40,"enabled":true,"title":"全部商品","productIds":[]},{"type":"RICH_TEXT","sortOrder":50,"enabled":true,"title":"售后说明","html":"<p>支持七天无理由（特例除外）</p>"}]'
WHERE code = 'full_showcase';
