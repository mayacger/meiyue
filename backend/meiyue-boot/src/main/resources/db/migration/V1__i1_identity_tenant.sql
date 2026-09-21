-- I1：账号 / 角色 / 入驻申请 / 租户 / 店铺 / 商家成员
-- 多租户：共享库 + tenant_id 行级隔离（规划 C6）
-- MVP：1 租户 : 1 店铺

CREATE TABLE user_accounts (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(64)  NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(128) NOT NULL,
    phone           VARCHAR(32),
    -- ENABLED / DISABLED
    status          VARCHAR(32)  NOT NULL DEFAULT 'ENABLED',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_user_accounts_username UNIQUE (username)
);

COMMENT ON TABLE user_accounts IS '平台级账号（买家/商家成员/平台管理员共用）';
COMMENT ON COLUMN user_accounts.password_hash IS 'BCrypt 哈希，明文永不明文入库';

-- 多角色：同一用户可同时具备 BUYER + SELLER_OWNER 等
CREATE TABLE user_roles (
    user_id     BIGINT       NOT NULL REFERENCES user_accounts (id) ON DELETE CASCADE,
    -- BUYER | SELLER_OWNER | SELLER_STAFF | PLATFORM_ADMIN
    role_code   VARCHAR(32)  NOT NULL,
    PRIMARY KEY (user_id, role_code)
);

COMMENT ON TABLE user_roles IS '用户角色绑定（RBAC）';

-- 入驻申请：审核通过后创建 tenant + store
CREATE TABLE onboarding_applications (
    id                  BIGSERIAL PRIMARY KEY,
    applicant_user_id   BIGINT       NOT NULL REFERENCES user_accounts (id),
    shop_name           VARCHAR(128) NOT NULL,
    shop_slug           VARCHAR(64)  NOT NULL,
    contact_name        VARCHAR(64)  NOT NULL,
    contact_phone       VARCHAR(32)  NOT NULL,
    -- PENDING | APPROVED | REJECTED
    status              VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    review_note         VARCHAR(512),
    reviewed_by         BIGINT REFERENCES user_accounts (id),
    reviewed_at         TIMESTAMPTZ,
    -- 审核通过后回填
    tenant_id           BIGINT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_onboarding_shop_slug UNIQUE (shop_slug)
);

CREATE INDEX idx_onboarding_status ON onboarding_applications (status);
CREATE INDEX idx_onboarding_applicant ON onboarding_applications (applicant_user_id);

COMMENT ON TABLE onboarding_applications IS '商家入驻申请；通过后开店（1:1）';

CREATE TABLE tenants (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(128) NOT NULL,
    -- ACTIVE | SUSPENDED
    status          VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE tenants IS '入驻商家租户';

CREATE TABLE stores (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL REFERENCES tenants (id),
    name            VARCHAR(128) NOT NULL,
    slug            VARCHAR(64)  NOT NULL,
    -- OPEN | CLOSED
    status          VARCHAR(32)  NOT NULL DEFAULT 'OPEN',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_stores_tenant UNIQUE (tenant_id),
    CONSTRAINT uk_stores_slug UNIQUE (slug)
);

COMMENT ON TABLE stores IS '店铺；MVP 与租户 1:1';

CREATE TABLE seller_members (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL REFERENCES tenants (id),
    user_id         BIGINT       NOT NULL REFERENCES user_accounts (id),
    -- OWNER | STAFF
    member_role     VARCHAR(32)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_seller_member UNIQUE (tenant_id, user_id)
);

CREATE INDEX idx_seller_members_user ON seller_members (user_id);

COMMENT ON TABLE seller_members IS '商家成员归属（用于注入 tenant_id，防串租）';
