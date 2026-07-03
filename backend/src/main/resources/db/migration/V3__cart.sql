-- V3: carts, cart_items

CREATE TABLE carts (
    id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    buyer_id    NUMBER    NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    CONSTRAINT carts_buyer_fk FOREIGN KEY (buyer_id) REFERENCES users(id),
    CONSTRAINT carts_buyer_uq UNIQUE (buyer_id)
);

CREATE TABLE cart_items (
    id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cart_id     NUMBER     NOT NULL,
    product_id  NUMBER     NOT NULL,
    quantity    NUMBER(3)  DEFAULT 1 NOT NULL,
    CONSTRAINT ci_cart_fk       FOREIGN KEY (cart_id)    REFERENCES carts(id)    ON DELETE CASCADE,
    CONSTRAINT ci_product_fk    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT ci_qty_ck        CHECK (quantity >= 1),
    CONSTRAINT ci_cart_prod_uq  UNIQUE (cart_id, product_id)
);
