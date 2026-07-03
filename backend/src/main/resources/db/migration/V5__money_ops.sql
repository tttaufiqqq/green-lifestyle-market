-- V5: refunds, payouts, payout_items

CREATE TABLE refunds (
    id            NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id      NUMBER             NOT NULL,
    requested_by  NUMBER             NOT NULL,
    reason        VARCHAR2(500 CHAR) NOT NULL,
    amount        NUMBER(10,2)       NOT NULL,
    status        VARCHAR2(20 CHAR)  DEFAULT 'REQUESTED' NOT NULL,
    admin_id      NUMBER             NULL,
    admin_note    VARCHAR2(255 CHAR) NULL,
    bank_ref      VARCHAR2(60 CHAR)  NULL,
    processed_at  TIMESTAMP          NULL,
    created_at    TIMESTAMP          NOT NULL,
    updated_at    TIMESTAMP          NOT NULL,
    CONSTRAINT ref_order_uq      UNIQUE (order_id),
    CONSTRAINT ref_order_fk      FOREIGN KEY (order_id)     REFERENCES orders(id),
    CONSTRAINT ref_requester_fk  FOREIGN KEY (requested_by) REFERENCES users(id),
    CONSTRAINT ref_admin_fk      FOREIGN KEY (admin_id)     REFERENCES users(id),
    CONSTRAINT ref_status_ck     CHECK (status IN ('REQUESTED','APPROVED','REJECTED','PROCESSED'))
);

CREATE TABLE payouts (
    id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payout_no   CHAR(14)          NOT NULL,
    seller_id   NUMBER            NOT NULL,
    amount      NUMBER(10,2)      NOT NULL,
    status      VARCHAR2(10 CHAR) DEFAULT 'PENDING' NOT NULL,
    admin_id    NUMBER            NULL,
    bank_ref    VARCHAR2(60 CHAR) NULL,
    paid_at     TIMESTAMP         NULL,
    created_at  TIMESTAMP         NOT NULL,
    updated_at  TIMESTAMP         NOT NULL,
    CONSTRAINT po_no_uq      UNIQUE (payout_no),
    CONSTRAINT po_seller_fk  FOREIGN KEY (seller_id) REFERENCES users(id),
    CONSTRAINT po_admin_fk   FOREIGN KEY (admin_id)  REFERENCES users(id),
    CONSTRAINT po_status_ck  CHECK (status IN ('PENDING','PAID','FAILED'))
);

CREATE TABLE payout_items (
    id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payout_id   NUMBER       NOT NULL,
    order_id    NUMBER       NOT NULL,
    amount      NUMBER(10,2) NOT NULL,
    CONSTRAINT poi_payout_fk  FOREIGN KEY (payout_id) REFERENCES payouts(id) ON DELETE CASCADE,
    CONSTRAINT poi_order_fk   FOREIGN KEY (order_id)  REFERENCES orders(id),
    CONSTRAINT poi_order_uq   UNIQUE (order_id)
);
