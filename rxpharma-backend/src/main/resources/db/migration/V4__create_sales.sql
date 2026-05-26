CREATE TYPE payment_method AS ENUM (
    'CASH',
    'CARD',
    'MOBILE_PAYMENT'
);

CREATE TABLE sales (
                       id              BIGSERIAL PRIMARY KEY,
                       invoice_number  VARCHAR(50)         NOT NULL UNIQUE,
                       cashier_id      BIGINT              REFERENCES users(id) ON DELETE SET NULL,
                       patient_name    VARCHAR(100),
                       total_amount    NUMERIC(10,2)       NOT NULL,
                       tax_amount      NUMERIC(10,2)       NOT NULL DEFAULT 0,
                       payment_method  payment_method      NOT NULL,
                       sale_date       TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE TABLE sale_items (
                            id          BIGSERIAL PRIMARY KEY,
                            sale_id     BIGINT          NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
                            drug_id     BIGINT          NOT NULL REFERENCES drugs(id) ON DELETE RESTRICT,
                            quantity    INTEGER         NOT NULL,
                            unit_price  NUMERIC(10,2)   NOT NULL,
                            subtotal    NUMERIC(10,2)   NOT NULL
);