-- I8：AI 素材库 + 商品封面挂接
-- 硬约束：不做推广视频、不做直播

CREATE TABLE media_assets (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL REFERENCES tenants (id),
    -- IMAGE | DETAIL_HTML（视频二期，本表暂不引入 VIDEO）
    asset_type      VARCHAR(32)  NOT NULL,
    -- 来源：AI_MOCK / AI_OPENAI / MANUAL_UPLOAD
    source          VARCHAR(32)  NOT NULL,
    -- 存储 URL 或本地相对路径；MOCK 可为 mock://...
    url             VARCHAR(1024) NOT NULL,
    -- 提示词 / 标题摘要（审计用，可空）
    prompt          VARCHAR(1024),
    -- 审核：PENDING / APPROVED / REJECTED
    moderation_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    moderation_note VARCHAR(512),
    -- 生成失败时保留原因，便于降级人工上传
    fail_reason     VARCHAR(512),
    created_by      BIGINT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_media_tenant ON media_assets (tenant_id, created_at DESC);
CREATE INDEX idx_media_moderation ON media_assets (moderation_status);

COMMENT ON TABLE media_assets IS 'I8 素材库：AI/人工图与详情；先审后用；无视频类型';

-- 商品封面挂接（人工上传或 AI 通过后写入）
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS cover_image_url VARCHAR(1024);

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS cover_asset_id BIGINT REFERENCES media_assets (id);

COMMENT ON COLUMN products.cover_image_url IS '封面图 URL；AI 失败时可人工填写';
COMMENT ON COLUMN products.cover_asset_id IS '关联已审核通过的素材 ID';
