-- V2: categories (+ seeds), products, product_images, Oracle Text CONTEXT index

CREATE TABLE categories (
    id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id   NUMBER           NULL,
    name        VARCHAR2(60 CHAR)  NOT NULL,
    slug        VARCHAR2(80 CHAR)  NOT NULL,
    is_active   BOOLEAN          DEFAULT TRUE NOT NULL,
    sort_order  NUMBER(3)        DEFAULT 0    NOT NULL,
    CONSTRAINT cat_slug_uq    UNIQUE (slug),
    CONSTRAINT cat_parent_fk  FOREIGN KEY (parent_id) REFERENCES categories(id)
);

INSERT INTO categories (name, slug, sort_order) VALUES ('Sustainable Stationery',        'sustainable-stationery',        1);
INSERT INTO categories (name, slug, sort_order) VALUES ('Reusable Bottles & Containers', 'reusable-bottles-containers',   2);
INSERT INTO categories (name, slug, sort_order) VALUES ('Organic Food',                  'organic-food',                  3);
INSERT INTO categories (name, slug, sort_order) VALUES ('Eco Electronics',               'eco-electronics',               4);
INSERT INTO categories (name, slug, sort_order) VALUES ('Sustainable Fashion',           'sustainable-fashion',           5);
INSERT INTO categories (name, slug, sort_order) VALUES ('Home & Living',                 'home-living',                   6);
INSERT INTO categories (name, slug, sort_order) VALUES ('Renewable Energy',              'renewable-energy',              7);
INSERT INTO categories (name, slug, sort_order) VALUES ('Books & Education',             'books-education',               8);
INSERT INTO categories (name, slug, sort_order) VALUES ('Others',                        'others',                        9);

CREATE TABLE products (
    id                  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    seller_id           NUMBER             NOT NULL,
    category_id         NUMBER             NOT NULL,
    title               VARCHAR2(120 CHAR) NOT NULL,
    slug                VARCHAR2(140 CHAR) NOT NULL,
    description         CLOB               NOT NULL,
    item_condition      VARCHAR2(10 CHAR)  NOT NULL,
    price               NUMBER(10,2)       NOT NULL,
    quantity            NUMBER(5)          DEFAULT 1 NOT NULL,
    allow_meetup        BOOLEAN            DEFAULT TRUE  NOT NULL,
    allow_shipping      BOOLEAN            DEFAULT FALSE NOT NULL,
    shipping_fee        NUMBER(10,2)       NULL,
    meetup_location     VARCHAR2(120 CHAR) NULL,
    sustainability_note VARCHAR2(255 CHAR) NULL,
    status              VARCHAR2(10 CHAR)  DEFAULT 'DRAFT' NOT NULL,
    created_at          TIMESTAMP          NOT NULL,
    updated_at          TIMESTAMP          NOT NULL,
    CONSTRAINT prod_slug_uq        UNIQUE (slug),
    CONSTRAINT prod_seller_fk      FOREIGN KEY (seller_id)   REFERENCES users(id),
    CONSTRAINT prod_category_fk    FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT prod_condition_ck   CHECK (item_condition IN ('NEW','LIKE_NEW','GOOD','FAIR')),
    CONSTRAINT prod_price_ck       CHECK (price >= 1.00),
    CONSTRAINT prod_quantity_ck    CHECK (quantity >= 0),
    CONSTRAINT prod_status_ck      CHECK (status IN ('DRAFT','ACTIVE','SOLD_OUT','SUSPENDED','DELETED')),
    CONSTRAINT prod_fulfilment_ck  CHECK (allow_meetup = TRUE OR allow_shipping = TRUE)
);

CREATE INDEX prod_status_cat_idx  ON products(status, category_id, created_at);
CREATE INDEX prod_seller_stat_idx ON products(seller_id, status);

CREATE TABLE product_images (
    id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id  NUMBER             NOT NULL,
    path        VARCHAR2(255 CHAR) NOT NULL,
    sort_order  NUMBER(2)          DEFAULT 0    NOT NULL,
    is_primary  BOOLEAN            DEFAULT FALSE NOT NULL,
    CONSTRAINT pi_product_fk FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX pi_product_sort_idx ON product_images(product_id, sort_order);

-- Oracle Text CONTEXT index (title + description, SYNC ON COMMIT)
BEGIN
    CTX_DDL.CREATE_PREFERENCE('glm_products_ds', 'MULTI_COLUMN_DATASTORE');
    CTX_DDL.SET_ATTRIBUTE('glm_products_ds', 'COLUMNS', 'title, description');
END;
/

CREATE INDEX products_search_ctx ON products(title)
    INDEXTYPE IS CTXSYS.CONTEXT
    PARAMETERS ('DATASTORE glm_products_ds SYNC (ON COMMIT)');
