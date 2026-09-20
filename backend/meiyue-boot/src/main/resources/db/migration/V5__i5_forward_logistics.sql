-- I5：正向物流全状态 + 轨迹
-- 状态：pending_pickup → picked_up → in_transit → out_for_delivery → delivered
-- 异常：exception | rejected | returned_to_sender

CREATE TABLE shipments (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL REFERENCES tenants (id),
    order_id            BIGINT       NOT NULL REFERENCES orders (id),
    -- 可选：按店拆包裹时关联订单行；空表示整单发货（简化）
    order_item_id       BIGINT,
    -- FORWARD | REVERSE
    direction           VARCHAR(16)  NOT NULL,
    -- 承运商编码占位，如 SF / YTO / ZTO
    carrier_code        VARCHAR(32),
    tracking_no         VARCHAR(64),
    -- 状态见注释
    status              VARCHAR(32)  NOT NULL,
    -- 收件人摘要（正向：买家；逆向：商家）
    receiver_name       VARCHAR(64),
    receiver_phone      VARCHAR(32),
    receiver_address    VARCHAR(512),
    -- 关联售后单（逆向时）
    aftersale_id        BIGINT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_shipments_order ON shipments (order_id);
CREATE INDEX idx_shipments_tenant ON shipments (tenant_id);
CREATE INDEX idx_shipments_tracking ON shipments (tracking_no);

COMMENT ON TABLE shipments IS '运单：正向/逆向；行级 tenant_id';

CREATE TABLE shipment_tracks (
    id                  BIGSERIAL PRIMARY KEY,
    shipment_id         BIGINT       NOT NULL REFERENCES shipments (id) ON DELETE CASCADE,
    status              VARCHAR(32)  NOT NULL,
    description         VARCHAR(512) NOT NULL,
    -- MANUAL | QUERY
    source              VARCHAR(16)  NOT NULL DEFAULT 'MANUAL',
    tracked_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tracks_shipment ON shipment_tracks (shipment_id);

COMMENT ON TABLE shipment_tracks IS '物流轨迹节点';

-- 订单状态扩展：PAID 后可 FULFILLING / COMPLETED（varchar 无需改约束）
