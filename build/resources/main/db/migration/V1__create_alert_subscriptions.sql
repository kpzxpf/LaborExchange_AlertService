CREATE TABLE alert_subscriptions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL,
    user_email      VARCHAR(255)  NOT NULL,
    keywords        VARCHAR(500),
    location        VARCHAR(255),
    min_salary      DOUBLE PRECISION,
    max_salary      DOUBLE PRECISION,
    employment_type VARCHAR(100),
    work_format     VARCHAR(100),
    skill_ids       TEXT,  -- comma-separated skill IDs e.g. "1,5,12"
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_as_user_id  ON alert_subscriptions (user_id);
CREATE INDEX idx_as_active   ON alert_subscriptions (is_active);
-- One subscription per user per keyword+location combo (allow multiple subscriptions)
