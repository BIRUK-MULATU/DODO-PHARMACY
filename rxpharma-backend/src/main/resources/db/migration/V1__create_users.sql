CREATE TYPE user_role AS ENUM (
    'ADMIN',
    'PHARMACIST',
    'CASHIER',
    'SUPPLIER_MANAGER'
);

CREATE TABLE users (
                       id          BIGSERIAL PRIMARY KEY,
                       full_name   VARCHAR(100)        NOT NULL,
                       email       VARCHAR(150)        NOT NULL UNIQUE,
                       password    VARCHAR(255)        NOT NULL,
                       role        user_role           NOT NULL,
                       created_at  TIMESTAMP           NOT NULL DEFAULT NOW()
);