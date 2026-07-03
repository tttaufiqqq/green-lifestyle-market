-- V1: users, verification_tokens, seller_bank_accounts, audit_logs

CREATE TABLE users (
    id                 NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name               VARCHAR2(100 CHAR)  NOT NULL,
    email              VARCHAR2(190 CHAR)  NOT NULL,
    password_hash      VARCHAR2(100 CHAR)  NOT NULL,
    phone              VARCHAR2(15 CHAR)   NULL,
    role               VARCHAR2(10 CHAR)   DEFAULT 'USER'   NOT NULL,
    affiliation        VARCHAR2(15 CHAR)   DEFAULT 'PUBLIC' NOT NULL,
    status             VARCHAR2(10 CHAR)   DEFAULT 'ACTIVE' NOT NULL,
    email_verified_at  TIMESTAMP           NULL,
    created_at         TIMESTAMP           NOT NULL,
    updated_at         TIMESTAMP           NOT NULL,
    CONSTRAINT users_email_uq        UNIQUE (email),
    CONSTRAINT users_role_ck         CHECK (role        IN ('USER','ADMIN')),
    CONSTRAINT users_affiliation_ck  CHECK (affiliation IN ('UTEM_STUDENT','UTEM_STAFF','PUBLIC')),
    CONSTRAINT users_status_ck       CHECK (status      IN ('ACTIVE','SUSPENDED'))
);

CREATE TABLE verification_tokens (
    id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     NUMBER        NOT NULL,
    token_hash  CHAR(64)      NOT NULL,
    purpose     VARCHAR2(20 CHAR) NOT NULL,
    expires_at  TIMESTAMP     NOT NULL,
    used_at     TIMESTAMP     NULL,
    CONSTRAINT vt_user_fk     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT vt_hash_uq     UNIQUE (token_hash),
    CONSTRAINT vt_purpose_ck  CHECK (purpose IN ('EMAIL_VERIFY','PASSWORD_RESET'))
);

CREATE TABLE seller_bank_accounts (
    id             NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id        NUMBER            NOT NULL,
    bank_name      VARCHAR2(50 CHAR) NOT NULL,
    account_no     VARCHAR2(20 CHAR) NOT NULL,
    holder_name    VARCHAR2(100 CHAR) NOT NULL,
    verified       BOOLEAN           DEFAULT FALSE NOT NULL,
    created_at     TIMESTAMP         NOT NULL,
    updated_at     TIMESTAMP         NOT NULL,
    CONSTRAINT sba_user_fk  FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT sba_user_uq  UNIQUE (user_id)
);

CREATE TABLE audit_logs (
    id           NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      NUMBER           NULL,
    action       VARCHAR2(60 CHAR) NOT NULL,
    entity_type  VARCHAR2(40 CHAR) NOT NULL,
    entity_id    NUMBER           NULL,
    meta         JSON             NULL,
    ip           VARCHAR2(45 CHAR) NULL,
    created_at   TIMESTAMP        NOT NULL,
    CONSTRAINT al_user_fk FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX al_user_created_idx ON audit_logs(user_id, created_at);
