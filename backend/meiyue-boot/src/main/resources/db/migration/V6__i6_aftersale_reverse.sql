-- I6：售后 + 逆向物流 + 48h 自动同意

CREATE TABLE aftersales (
    id                  BIGSERIAL PRIMARY KEY,
    aftersale_no        VARCHAR(32)  NOT NULL UNIQUE,
    order_id            BIGINT       NOT NULL REFERENCES orders (id),
    order_item_id       BIGINT,
    tenant_id           BIGINT       NOT NULL REFERENCES tenants (id),
    buyer_user_id       BIGINT       NOT NULL REFERENCES user_accounts (id),
    -- REFUND_ONLY | RETURN_REFUND
    type                VARCHAR(32)  NOT NULL,
    -- applied → reviewing → approved|rejected → refunding → closed
    status              VARCHAR(32)  NOT NULL,
    reason              VARCHAR(512) NOT NULL,
    refund_cents        BIGINT       NOT NULL,
    -- 商家审核截止（创建后 + 48h）
    seller_deadline_at  TIMESTAMPTZ  NOT NULL,
    reviewed_at         TIMESTAMPTZ,
    review_note         VARCHAR(512),
    -- 逆向运单（退货退款）
    reverse_shipment_id BIGINT REFERENCES shipments (id),
    closed_at           TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_aftersales_tenant_status ON aftersales (tenant_id, status);
CREATE INDEX idx_aftersales_buyer ON aftersales (buyer_user_id);
CREATE INDEX idx_aftersales_deadline ON aftersales (status, seller_deadline_at);

COMMENT ON TABLE aftersales IS '售后单；48h 未审自动同意';

-- shipments.aftersale_id 外键（V5 已有列，此处补 FK 若需要）
-- 使用 NOT VALID 避免历史空值问题；PG 可直接 ADD
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_shipments_aftersale'
  ) THEN
    ALTER TABLE shipments
      ADD CONSTRAINT fk_shipments_aftersale
      FOREIGN KEY (aftersale_id) REFERENCES aftersales (id);
  END IF;
EXCEPTION WHEN others THEN
  -- aftersales 刚创建，循环引用：先不加 FK 或延后
  RAISE NOTICE 'skip fk_shipments_aftersale: %', SQLERRM;
END $$;
