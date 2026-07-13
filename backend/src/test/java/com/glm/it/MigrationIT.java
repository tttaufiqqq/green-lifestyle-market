package com.glm.it;

import oracle.jdbc.pool.OracleDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MigrationIT {

    // Targets glm_app_dev, never glm_app (prod) — this test drops and recreates the
    // user on every run, so it must never point at the schema the deployed app uses.
    // See docs/01-oracle/glm-db-access.md in the homelab repo.
    static final String DB_URL   = System.getenv().getOrDefault("IT_DB_URL",      "jdbc:oracle:thin:@100.118.110.114:1521/FREEPDB1");
    static final String SYS_PASS = System.getenv().getOrDefault("IT_DB_SYS_PASS", "qwertY1612");
    static final String APP_USER = "glm_app_dev";
    static final String APP_PASS = "GlmTest_1!";
    static final String FDA_NAME = "glm_fda_dev";

    static Connection conn;

    @BeforeAll
    static void setUp() throws Exception {
        // ── 1. SYS: drop stale objects, recreate glm_app_dev ──────────────────
        Properties sysProp = new Properties();
        sysProp.setProperty("internal_logon", "sysdba");
        OracleDataSource sysDs = new OracleDataSource();
        sysDs.setURL(DB_URL); sysDs.setUser("sys"); sysDs.setPassword(SYS_PASS);
        sysDs.setConnectionProperties(sysProp);

        try (Connection sysConn = sysDs.getConnection()) {
            execIgnore(sysConn, "DROP FLASHBACK ARCHIVE " + FDA_NAME);
            execIgnore(sysConn, "DROP USER " + APP_USER + " CASCADE");
            exec(sysConn, "CREATE USER " + APP_USER + " IDENTIFIED BY \"" + APP_PASS + "\"" +
                          " DEFAULT TABLESPACE users TEMPORARY TABLESPACE temp QUOTA UNLIMITED ON users");
            exec(sysConn, "GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE," +
                          " CREATE PROCEDURE, CREATE TRIGGER, CREATE VIEW TO " + APP_USER);
            exec(sysConn, "GRANT CTXAPP TO " + APP_USER);
            exec(sysConn, "GRANT FLASHBACK ARCHIVE ADMINISTER TO " + APP_USER);
        }

        // ── 2. glm_app_dev: run Flyway migrations + seed ──────────────────────
        OracleDataSource appDs = new OracleDataSource();
        appDs.setURL(DB_URL); appDs.setUser(APP_USER); appDs.setPassword(APP_PASS);

        Flyway.configure()
                .dataSource(appDs)
                .locations("classpath:db/migration", "classpath:db/seed")
                .placeholders(Map.of("flashbackArchiveName", FDA_NAME))
                .load()
                .migrate();

        conn = appDs.getConnection();
        conn.setAutoCommit(false);
    }

    private static void exec(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement()) { s.execute(sql); }
    }

    private static void execIgnore(Connection c, String sql) {
        try (Statement s = c.createStatement()) { s.execute(sql); } catch (SQLException ignored) {}
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) conn.close();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private long queryLong(String sql) throws SQLException {
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            assertTrue(rs.next());
            return rs.getLong(1);
        }
    }

    private long queryLong(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test @org.junit.jupiter.api.Order(1)
    void categoriesSeedRowsExist() throws Exception {
        long count = queryLong("SELECT COUNT(*) FROM categories");
        assertTrue(count > 0, "V2 seed must insert categories");
    }

    @Test @org.junit.jupiter.api.Order(2)
    void duplicateWebhookIdempotencyKeyFails() throws Exception {
        String insertSql =
            "INSERT INTO webhook_events (source, idempotency_key, raw_payload, processed, created_at) " +
            "VALUES ('CALLBACK', 'bill-dup:ref-dup:1', JSON('{\"k\":1}'), FALSE, SYSTIMESTAMP)";

        try (Statement s = conn.createStatement()) {
            s.executeUpdate(insertSql);
            conn.commit();

            assertThrows(SQLException.class, () -> {
                try (Statement s2 = conn.createStatement()) {
                    s2.executeUpdate(insertSql);
                    conn.commit();
                }
            }, "Duplicate idempotency_key must raise a constraint violation");
        }
        conn.rollback();
    }

    @Test @org.junit.jupiter.api.Order(3)
    void duplicatePayoutItemOrderIdFails() throws Exception {
        long buyerId  = queryLong("SELECT id FROM users WHERE email = 'admin@glm.dev'");
        long sellerId = queryLong("SELECT id FROM users WHERE email = 'seller@glm.dev'");
        // minimal payment
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO payments (payment_no, buyer_id, amount, expires_at, created_at, updated_at) " +
                "VALUES ('PAY-TEST-00001', ?, 10, SYSTIMESTAMP+1, SYSTIMESTAMP, SYSTIMESTAMP)")) {
            ps.setLong(1, buyerId);
            ps.executeUpdate();
        }
        long paymentId = queryLong("SELECT id FROM payments WHERE payment_no = 'PAY-TEST-00001'");

        // minimal order
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO orders (order_no, payment_id, buyer_id, seller_id, status, " +
                "fulfilment_method, subtotal, shipping_fee, total, platform_fee, seller_net, " +
                "created_at, updated_at) VALUES ('ORD-TEST-00001',?,?,?,'PAID','MEETUP',10,0,10,0.5,9.5,SYSTIMESTAMP,SYSTIMESTAMP)")) {
            ps.setLong(1, paymentId); ps.setLong(2, buyerId); ps.setLong(3, sellerId);
            ps.executeUpdate();
        }
        long orderId = queryLong("SELECT id FROM orders WHERE order_no = 'ORD-TEST-00001'");

        // minimal payout
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO payouts (payout_no, seller_id, amount, created_at, updated_at) " +
                "VALUES ('PO-TEST-0001', ?, 9.5, SYSTIMESTAMP, SYSTIMESTAMP)")) {
            ps.setLong(1, sellerId);
            ps.executeUpdate();
        }
        long payoutId = queryLong("SELECT id FROM payouts WHERE payout_no = 'PO-TEST-0001'");

        // first payout_item — must succeed
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO payout_items (payout_id, order_id, amount) VALUES (?,?,9.5)")) {
            ps.setLong(1, payoutId); ps.setLong(2, orderId);
            ps.executeUpdate();
        }
        conn.commit();

        // second payout_item same order — must fail
        assertThrows(SQLException.class, () -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO payout_items (payout_id, order_id, amount) VALUES (?,?,9.5)")) {
                ps.setLong(1, payoutId); ps.setLong(2, orderId);
                ps.executeUpdate();
                conn.commit();
            }
        }, "Duplicate payout_items.order_id must raise a constraint violation");
        conn.rollback();
    }

    @Test @org.junit.jupiter.api.Order(4)
    void productPriceBelowMinimumFails() throws Exception {
        long sellerId = queryLong("SELECT id FROM users WHERE email = 'seller@glm.dev'");
        long catId    = queryLong("SELECT id FROM categories WHERE rownum = 1");

        assertThrows(SQLException.class, () -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO products (seller_id, category_id, title, slug, description, " +
                    "item_condition, price, quantity, allow_meetup, allow_shipping, status, " +
                    "created_at, updated_at) VALUES (?,?,'Bad Price','bad-price-1','desc','NEW'," +
                    "0.50,1,TRUE,FALSE,'ACTIVE',SYSTIMESTAMP,SYSTIMESTAMP)")) {
                ps.setLong(1, sellerId); ps.setLong(2, catId);
                ps.executeUpdate();
                conn.commit();
            }
        }, "price < 1.00 must violate prod_price_ck CHECK constraint");
        conn.rollback();
    }

    @Test @org.junit.jupiter.api.Order(5)
    void containsSearchReturnsSeededProduct() throws Exception {
        // Force Oracle Text index sync before querying
        try (Statement s = conn.createStatement()) {
            s.execute("BEGIN CTX_DDL.SYNC_INDEX('products_search_ctx'); END;");
            conn.commit();
        }
        long count = queryLong(
            "SELECT COUNT(*) FROM products WHERE CONTAINS(title, 'bamboo', 1) > 0");
        assertTrue(count > 0, "CONTAINS search must find seeded 'Eco Bamboo Notebook'");
    }
}
