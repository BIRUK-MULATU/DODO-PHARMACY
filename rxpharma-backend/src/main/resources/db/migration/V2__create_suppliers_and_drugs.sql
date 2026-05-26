CREATE TYPE supplier_status AS ENUM (
    'ACTIVE',
    'ON_HOLD'
);

CREATE TABLE suppliers (
                           id              BIGSERIAL PRIMARY KEY,
                           company_name    VARCHAR(150)        NOT NULL,
                           contact_person  VARCHAR(100)        NOT NULL,
                           email           VARCHAR(150)        NOT NULL UNIQUE,
                           phone           VARCHAR(20)         NOT NULL,
                           status          supplier_status     NOT NULL DEFAULT 'ACTIVE',
                           created_at      TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE TABLE drugs (
                       id              BIGSERIAL PRIMARY KEY,
                       name            VARCHAR(150)        NOT NULL,
                       sku             VARCHAR(50)         NOT NULL UNIQUE,
                       category        VARCHAR(100)        NOT NULL,
                       price           NUMERIC(10,2)       NOT NULL,
                       stock_qty       INTEGER             NOT NULL DEFAULT 0,
                       expiry_date     DATE                NOT NULL,
                       supplier_id     BIGINT              REFERENCES suppliers(id) ON DELETE SET NULL,
                       created_at      TIMESTAMP           NOT NULL DEFAULT NOW()
);