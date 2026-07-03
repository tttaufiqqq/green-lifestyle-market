-- oracle-provision.sql
-- Run ONCE as a DBA (SYSDBA) before first application deployment.
-- Creates the GLM_APP schema user and grants required privileges.
-- NOT managed by Flyway — execute manually: sqlplus sys/...@FREE as sysdba @oracle-provision.sql

-- ── Schema user ──────────────────────────────────────────────────────────────
CREATE USER glm_app IDENTIFIED BY "ChangeMe_Strong1!"
    DEFAULT TABLESPACE users
    TEMPORARY TABLESPACE temp
    QUOTA UNLIMITED ON users;

-- Basic connect + DDL (Flyway runs as this user in production)
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE,
      CREATE PROCEDURE, CREATE TRIGGER, CREATE VIEW TO glm_app;

-- ── Oracle Text ───────────────────────────────────────────────────────────────
-- Required for CTX_DDL calls and CONTEXT index creation in V2 + V6
GRANT CTXAPP TO glm_app;

-- ── Flashback Data Archive ────────────────────────────────────────────────────
-- Required for CREATE FLASHBACK ARCHIVE and ALTER TABLE ... FLASHBACK ARCHIVE in V6
GRANT FLASHBACK ARCHIVE ADMINISTER TO glm_app;

-- ── Audit log append-only access ─────────────────────────────────────────────
-- After Flyway runs, restrict audit_logs to INSERT + SELECT only.
-- (Run this section after initial Flyway migrate completes.)
-- GRANT INSERT, SELECT ON glm_app.audit_logs TO glm_app;
-- REVOKE UPDATE, DELETE ON glm_app.audit_logs FROM glm_app;
