CREATE TABLE purchase_order_items (
    id                 BIGSERIAL PRIMARY KEY,
    purchase_order_id  BIGINT         NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    drug_id            BIGINT         NOT NULL REFERENCES drugs(id) ON DELETE RESTRICT,
    quantity           INTEGER        NOT NULL,
    unit_cost          NUMERIC(10,2)  NOT NULL
);
