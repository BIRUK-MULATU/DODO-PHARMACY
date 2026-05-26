CREATE TYPE prescription_status AS ENUM (
    'PENDING',
    'DISPENSED',
    'CANCELLED'
);

CREATE TABLE prescriptions (
                               id              BIGSERIAL PRIMARY KEY,
                               patient_name    VARCHAR(100)            NOT NULL,
                               doctor_name     VARCHAR(100)            NOT NULL,
                               status          prescription_status     NOT NULL DEFAULT 'PENDING',
                               issued_date     DATE                    NOT NULL,
                               dispensed_by    BIGINT                  REFERENCES users(id) ON DELETE SET NULL,
                               notes           TEXT,
                               created_at      TIMESTAMP               NOT NULL DEFAULT NOW()
);

CREATE TABLE prescription_drugs (
                                    id                      BIGSERIAL PRIMARY KEY,
                                    prescription_id         BIGINT  NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,
                                    drug_id                 BIGINT  NOT NULL REFERENCES drugs(id) ON DELETE RESTRICT,
                                    quantity                INTEGER NOT NULL,
                                    dosage_instructions     TEXT
);