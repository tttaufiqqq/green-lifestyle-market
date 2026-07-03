# ADR-0002: Oracle Database 23ai Free over MariaDB, MySQL, SQL Server

## Context
DB required for a transactional marketplace with strict money/stock consistency, deployed on modest Linux hosts (Proxmox VM / VPS). Candidates: Oracle, MariaDB, MySQL, SQL Server. The developer is concurrently studying Oracle Database Foundations (1Z0-006), making Oracle hands-on experience a project goal in itself.

## Decision
Oracle Database 23ai Free (pluggable DB `FREE`, charset AL32UTF8), accessed via Spring Data JPA (Hibernate `OracleDialect`, `ojdbc11` driver), schema managed by Flyway.

## Rationale
- **Learning alignment**: directly reinforces 1Z0-006 study (identity columns, sequences, PL/SQL exposure, Oracle Text, Flashback) with a real project.
- **Capability**: everything the design needs exists in Free edition — row locking (`SELECT ... FOR UPDATE`), CHECK constraints as enum replacement, native JSON and BOOLEAN types (23ai), Oracle Text for catalog search, Flashback Data Archive for payments/orders/payouts row history.
- **MariaDB/MySQL**: perfectly adequate technically (and were the default choice), but add no new learning and no cert leverage.
- **SQL Server**: Express caps (10 GB / 1.4 GB buffer pool), Windows-first tooling, already abandoned once in a prior project.

## Consequences
+ Zero licensing cost at this scale; cert-aligned skills; enterprise features (FDA, Oracle Text) without extra components.
− Free-edition ceilings: 12 GB user data, 2 GB RAM, 2 CPU threads per instance — ample for campus scale, but a hard wall; migration path is licensed Oracle or a JPA-eased hop back to MariaDB/PostgreSQL.
− Heavier footprint: large binary install, slower CI cold-start.
− Oracle-specific SQL in migrations (VARCHAR2, CLOB, identity, CONTAINS()) reduces portability; acceptable because Flyway isolates all DDL and JPA isolates most queries.
- Supersedes the earlier MariaDB decision made in this planning phase; no code existed, so no migration cost.
