--liquibase formatted sql
--changeset nokk:002-create-refresh-tokens-table

CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id         UUID        PRIMARY KEY,
    account_id UUID        NOT NULL REFERENCES accounts (id),
    token_hash  TEXT        NOT NULL UNIQUE,
    is_active  BOOLEAN     NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

--rollback DROP TABLE IF EXISTS refresh_tokens;