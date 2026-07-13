-- V6: articles, notifications, push_subscriptions, Oracle Text (articles), GLM_FDA

CREATE TABLE articles (
    id           NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author_id    NUMBER             NOT NULL,
    title        VARCHAR2(150 CHAR) NOT NULL,
    slug         VARCHAR2(170 CHAR) NOT NULL,
    excerpt      VARCHAR2(300 CHAR) NOT NULL,
    body_md      CLOB               NOT NULL,
    cover_image  VARCHAR2(255 CHAR) NULL,
    status       VARCHAR2(10 CHAR)  DEFAULT 'DRAFT' NOT NULL,
    published_at TIMESTAMP          NULL,
    created_at   TIMESTAMP          NOT NULL,
    updated_at   TIMESTAMP          NOT NULL,
    CONSTRAINT art_slug_uq    UNIQUE (slug),
    CONSTRAINT art_author_fk  FOREIGN KEY (author_id) REFERENCES users(id),
    CONSTRAINT art_status_ck  CHECK (status IN ('DRAFT','PUBLISHED'))
);

BEGIN
    CTX_DDL.CREATE_PREFERENCE('glm_articles_ds', 'MULTI_COLUMN_DATASTORE');
    CTX_DDL.SET_ATTRIBUTE('glm_articles_ds', 'COLUMNS', 'title, body_md');
END;
/

CREATE INDEX articles_search_ctx ON articles(title)
    INDEXTYPE IS CTXSYS.CONTEXT
    PARAMETERS ('DATASTORE glm_articles_ds SYNC (ON COMMIT)');

CREATE TABLE notifications (
    id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     NUMBER             NOT NULL,
    type        VARCHAR2(40 CHAR)  NOT NULL,
    title       VARCHAR2(120 CHAR) NOT NULL,
    body        VARCHAR2(255 CHAR) NOT NULL,
    data        JSON               NULL,
    read_at     TIMESTAMP          NULL,
    created_at  TIMESTAMP          NOT NULL,
    CONSTRAINT notif_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX notif_user_read_idx ON notifications(user_id, read_at);

CREATE TABLE push_subscriptions (
    id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     NUMBER             NOT NULL,
    endpoint    VARCHAR2(500 CHAR) NOT NULL,
    p256dh      VARCHAR2(120 CHAR) NOT NULL,
    auth        VARCHAR2(40 CHAR)  NOT NULL,
    created_at  TIMESTAMP          NOT NULL,
    CONSTRAINT ps_user_fk     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ps_endpoint_uq UNIQUE (endpoint)
);

-- Flashback Data Archive: 7-year row history for payments, orders, payouts
-- Name is a Flyway placeholder, not a literal: FDA names are unique per PDB (not per-schema),
-- so dev (glm_app_dev) and prod (glm_app) must use different names to avoid colliding.
-- Set via spring.flyway.placeholders.flashback-archive-name (glm_fda in prod, glm_fda_dev in dev).
CREATE FLASHBACK ARCHIVE ${flashbackArchiveName}
    TABLESPACE users
    QUOTA 2G
    RETENTION 7 YEAR;

ALTER TABLE payments FLASHBACK ARCHIVE ${flashbackArchiveName};
ALTER TABLE orders   FLASHBACK ARCHIVE ${flashbackArchiveName};
ALTER TABLE payouts  FLASHBACK ARCHIVE ${flashbackArchiveName};
