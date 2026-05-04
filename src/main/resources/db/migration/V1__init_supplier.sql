-- =========================================================
-- V1: Initial Supplier table
-- =========================================================
CREATE TABLE IF NOT EXISTS suppliers (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    phone_number    VARCHAR(30),
    company_name    VARCHAR(150),
    address         VARCHAR(255),
    country         VARCHAR(100),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    CONSTRAINT uk_supplier_email UNIQUE (email)
);

CREATE INDEX IF NOT EXISTS idx_supplier_name    ON suppliers (name);
CREATE INDEX IF NOT EXISTS idx_supplier_country ON suppliers (country);
CREATE INDEX IF NOT EXISTS idx_supplier_active  ON suppliers (active);
