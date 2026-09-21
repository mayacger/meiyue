-- I7：审计日志骨架（可观测/安全基线）
-- 记录关键写操作：谁、何时、对哪个租户、做了什么

CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    -- 操作者用户 ID（可空：系统任务）
    actor_user_id   BIGINT,
    -- 操作者类型：BUYER / SELLER / PLATFORM_ADMIN / SYSTEM / ANONYMOUS
    actor_type      VARCHAR(32)  NOT NULL,
    -- 涉及租户（可空：平台级操作）
    tenant_id       BIGINT,
    -- 动作码，如 PRODUCT_CREATE / PAYMENT_NOTIFY / AFTERSALE_APPROVE
    action          VARCHAR(64)  NOT NULL,
    -- 资源类型与 ID
    resource_type   VARCHAR(64),
    resource_id     VARCHAR(64),
    -- 结果：SUCCESS / FAIL
    outcome         VARCHAR(16)  NOT NULL DEFAULT 'SUCCESS',
    -- 补充细节（JSON 或短文本，禁止写密钥）
    detail          VARCHAR(1024),
    -- 请求追踪
    trace_id        VARCHAR(64),
    ip              VARCHAR(64),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_tenant_time ON audit_logs (tenant_id, created_at DESC);
CREATE INDEX idx_audit_action_time ON audit_logs (action, created_at DESC);
CREATE INDEX idx_audit_trace ON audit_logs (trace_id);

COMMENT ON TABLE audit_logs IS 'I7 审计日志骨架：关键写操作可追溯；不含密钥明文';
