-- I28/I29：首页运营 Banner + 售后凭证图 URL
-- 无直播组件；结算仍复用 settlement_ledgers（无真实分账）

CREATE TABLE platform_banners (
    id              BIGSERIAL PRIMARY KEY,
    -- 运营位标题（后台展示 / 无障碍 alt）
    title           VARCHAR(128)  NOT NULL,
    -- 图片 URL（占位，非强制 OSS）
    image_url       VARCHAR(1024) NOT NULL,
    -- 点击跳转：站内路径或外链
    link_url        VARCHAR(1024),
    sort_order      INT           NOT NULL DEFAULT 0,
    enabled         BOOLEAN       NOT NULL DEFAULT TRUE,
    -- 可选投放窗口；NULL 表示不限制
    start_at        TIMESTAMPTZ,
    end_at          TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_platform_banners_enabled_sort ON platform_banners (enabled, sort_order DESC, id DESC);

COMMENT ON TABLE platform_banners IS '平台首页运营 Banner（I28）；禁直播组件';

-- I29：售后凭证图 URL 列表（逗号分隔或 JSON 数组字符串）
ALTER TABLE aftersales
    ADD COLUMN IF NOT EXISTS evidence_image_urls VARCHAR(2048);

COMMENT ON COLUMN aftersales.evidence_image_urls IS '售后凭证图 URL，逗号分隔（I29）';
