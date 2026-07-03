-- V4: payments, orders, order_items, stock_reservations, webhook_events

CREATE TABLE payments (
    id                    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payment_no            CHAR(15)           NOT NULL,
    buyer_id              NUMBER             NOT NULL,
    amount                NUMBER(10,2)       NOT NULL,
    status                VARCHAR2(10 CHAR)  DEFAULT 'PENDING' NOT NULL,
    toyyibpay_bill_code   VARCHAR2(20 CHAR)  NULL,
    toyyibpay_ref_no      VARCHAR2(30 CHAR)  NULL,
    verified_at           TIMESTAMP          NULL,
    expires_at            TIMESTAMP          NOT NULL,
    paid_at               TIMESTAMP          NULL,
    created_at            TIMESTAMP          NOT NULL,
    updated_at            TIMESTAMP          NOT NULL,
    CONSTRAINT pay_no_uq        UNIQUE (payment_no),
    CONSTRAINT pay_bill_code_uq UNIQUE (toyyibpay_bill_code),
    CONSTRAINT pay_buyer_fk     FOREIGN KEY (buyer_id) REFERENCES users(id),
    CONSTRAINT pay_status_ck    CHECK (status IN ('PENDING','SUCCESS','FAILED','EXPIRED','REVIEW'))
);

CREATE TABLE orders (
    id               NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_no         CHAR(15)            NOT NULL,
    payment_id       NUMBER              NOT NULL,
    buyer_id         NUMBER              NOT NULL,
    seller_id        NUMBER              NOT NULL,
    status           VARCHAR2(20 CHAR)   NOT NULL,
    fulfilment_method VARCHAR2(10 CHAR)  NOT NULL,
    ship_name        VARCHAR2(100 CHAR)  NULL,
    ship_phone       VARCHAR2(20 CHAR)   NULL,
    ship_address1    VARCHAR2(255 CHAR)  NULL,
    ship_address2    VARCHAR2(255 CHAR)  NULL,
    ship_postcode    VARCHAR2(10 CHAR)   NULL,
    ship_city        VARCHAR2(80 CHAR)   NULL,
    ship_state       VARCHAR2(40 CHAR)   NULL,
    meetup_location  VARCHAR2(120 CHAR)  NULL,
    meetup_note      VARCHAR2(255 CHAR)  NULL,
    tracking_no      VARCHAR2(40 CHAR)   NULL,
    courier          VARCHAR2(40 CHAR)   NULL,
    subtotal         NUMBER(10,2)        NOT NULL,
    shipping_fee     NUMBER(10,2)        NOT NULL,
    total            NUMBER(10,2)        NOT NULL,
    platform_fee     NUMBER(10,2)        NOT NULL,
    seller_net       NUMBER(10,2)        NOT NULL,
    cancelled_reason VARCHAR2(255 CHAR)  NULL,
    confirmed_at     TIMESTAMP           NULL,
    shipped_at       TIMESTAMP           NULL,
    completed_at     TIMESTAMP           NULL,
    cancelled_at     TIMESTAMP           NULL,
    created_at       TIMESTAMP           NOT NULL,
    updated_at       TIMESTAMP           NOT NULL,
    CONSTRAINT ord_no_uq          UNIQUE (order_no),
    CONSTRAINT ord_payment_fk     FOREIGN KEY (payment_id) REFERENCES payments(id),
    CONSTRAINT ord_buyer_fk       FOREIGN KEY (buyer_id)   REFERENCES users(id),
    CONSTRAINT ord_seller_fk      FOREIGN KEY (seller_id)  REFERENCES users(id),
    CONSTRAINT ord_status_ck      CHECK (status IN ('PENDING_PAYMENT','PAID','CONFIRMED','SHIPPED',
                                   'READY_FOR_MEETUP','COMPLETED','CANCELLED','EXPIRED',
                                   'REFUND_REQUESTED','REFUNDED')),
    CONSTRAINT ord_fulfilment_ck  CHECK (fulfilment_method IN ('MEETUP','SHIPPING'))
);

CREATE INDEX ord_buyer_stat_idx   ON orders(buyer_id, status);
CREATE INDEX ord_seller_stat_idx  ON orders(seller_id, status);
CREATE INDEX ord_payment_idx      ON orders(payment_id);
CREATE INDEX ord_status_time_idx  ON orders(status, created_at);

CREATE TABLE order_items (
    id                  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id            NUMBER             NOT NULL,
    product_id          NUMBER             NOT NULL,
    title_snapshot      VARCHAR2(120 CHAR) NOT NULL,
    condition_snapshot  VARCHAR2(10 CHAR)  NOT NULL,
    unit_price          NUMBER(10,2)       NOT NULL,
    quantity            NUMBER(3)          NOT NULL,
    line_total          NUMBER(10,2)       NOT NULL,
    CONSTRAINT oi_order_fk   FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE,
    CONSTRAINT oi_product_fk FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE stock_reservations (
    id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id  NUMBER            NOT NULL,
    order_id    NUMBER            NOT NULL,
    quantity    NUMBER(3)         NOT NULL,
    status      VARCHAR2(10 CHAR) DEFAULT 'HELD' NOT NULL,
    expires_at  TIMESTAMP         NOT NULL,
    created_at  TIMESTAMP         NOT NULL,
    CONSTRAINT sr_product_fk FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT sr_order_fk   FOREIGN KEY (order_id)   REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT sr_status_ck  CHECK (status IN ('HELD','CONSUMED','RELEASED'))
);

CREATE INDEX sr_status_exp_idx ON stock_reservations(status, expires_at);

CREATE TABLE webhook_events (
    id               NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payment_id       NUMBER             NULL,
    source           VARCHAR2(10 CHAR)  NOT NULL,
    idempotency_key  VARCHAR2(80 CHAR)  NOT NULL,
    raw_payload      JSON               NOT NULL,
    processed        BOOLEAN            DEFAULT FALSE NOT NULL,
    processed_at     TIMESTAMP          NULL,
    created_at       TIMESTAMP          NOT NULL,
    CONSTRAINT we_idem_uq    UNIQUE (idempotency_key),
    CONSTRAINT we_payment_fk FOREIGN KEY (payment_id) REFERENCES payments(id),
    CONSTRAINT we_source_ck  CHECK (source IN ('CALLBACK','RETURN','QUERY'))
);
