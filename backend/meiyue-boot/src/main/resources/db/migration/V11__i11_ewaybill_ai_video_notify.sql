-- I11：电子面单/多包裹字段、AI 推广视频任务与商品挂接
-- 不做官方分账打款；不做直播

-- ========== 运单：包裹序号 + 电子面单占位 ==========
ALTER TABLE shipments
    ADD COLUMN IF NOT EXISTS package_seq INT NOT NULL DEFAULT 1;

ALTER TABLE shipments
    ADD COLUMN IF NOT EXISTS ewaybill_no VARCHAR(64);

ALTER TABLE shipments
    ADD COLUMN IF NOT EXISTS ewaybill_label_url VARCHAR(1024);

ALTER TABLE shipments
    ADD COLUMN IF NOT EXISTS ewaybill_provider VARCHAR(32);

COMMENT ON COLUMN shipments.package_seq IS '同一订单下包裹序号（多运单拆包）';
COMMENT ON COLUMN shipments.ewaybill_no IS '电子面单号（MOCK 打单生成）';
COMMENT ON COLUMN shipments.ewaybill_label_url IS '面单打印占位 URL（mock://ewaybill/...）';
COMMENT ON COLUMN shipments.ewaybill_provider IS '面单通道：MOCK 等';

-- ========== AI 推广视频异步任务 ==========
CREATE TABLE ai_video_tasks (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL REFERENCES tenants (id),
    -- 可选：生成后挂到商品
    product_id          BIGINT       REFERENCES products (id),
    -- PENDING | RUNNING | SUCCEEDED | FAILED
    status              VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    prompt              VARCHAR(1024) NOT NULL,
    result_asset_id     BIGINT       REFERENCES media_assets (id),
    fail_reason         VARCHAR(512),
    created_by          BIGINT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_video_tenant ON ai_video_tasks (tenant_id, created_at DESC);
CREATE INDEX idx_ai_video_status ON ai_video_tasks (status);

COMMENT ON TABLE ai_video_tasks IS 'I11 AI 推广视频异步任务；默认 MOCK；非直播';

-- 商品挂接推广视频（非直播）
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS promo_video_url VARCHAR(1024);

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS promo_video_asset_id BIGINT REFERENCES media_assets (id);

COMMENT ON COLUMN products.promo_video_url IS '推广视频 URL（MOCK 或审核通过素材）；非直播';
COMMENT ON COLUMN products.promo_video_asset_id IS '关联 VIDEO 类型素材';

COMMENT ON TABLE media_assets IS '素材库：IMAGE / DETAIL_HTML / VIDEO（推广短视频，非直播）';
