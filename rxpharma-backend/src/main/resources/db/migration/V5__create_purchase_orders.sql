CREATE TYPE order_status AS ENUM ('DRAFT', 'SENT', 'DELIVERED', 'CANCELLED');

CREATE TABLE purchase_orders (
                                 id              BIGSERIAL PRIMARY KEY,
                                 supplier_id     BIGINT         NOT NULL REFERENCES suppliers(id) ON DELETE RESTRICT,
                                 ordered_by      BIGINT         REFERENCES users(id) ON DELETE SET NULL,
                                 status          order_status   NOT NULL DEFAULT 'DRAFT',
                                 total_cost      NUMERIC(10,2)  NOT NULL,
                                 order_date      TIMESTAMP      NOT NULL DEFAULT NOW(),
                                 delivery_date   DATE
);