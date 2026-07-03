-- Dev seed: admin, test seller, sample products.
-- Repeatable migration; safe to re-run (MERGE is idempotent).
-- All test users have password: "password" (BCrypt $2a$10$ cost 10)

MERGE INTO users u
USING (SELECT 'admin@glm.dev' AS email FROM DUAL) src
ON (u.email = src.email)
WHEN NOT MATCHED THEN INSERT
    (name, email, password_hash, role, status, email_verified_at, created_at, updated_at)
VALUES
    ('GLM Admin', 'admin@glm.dev',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
     'ADMIN', 'ACTIVE', SYSTIMESTAMP, SYSTIMESTAMP, SYSTIMESTAMP);

MERGE INTO users u
USING (SELECT 'seller@glm.dev' AS email FROM DUAL) src
ON (u.email = src.email)
WHEN NOT MATCHED THEN INSERT
    (name, email, password_hash, role, status, email_verified_at, created_at, updated_at)
VALUES
    ('Test Seller', 'seller@glm.dev',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
     'USER', 'ACTIVE', SYSTIMESTAMP, SYSTIMESTAMP, SYSTIMESTAMP);

MERGE INTO products p
USING (SELECT 'eco-bamboo-notebook-1' AS slug FROM DUAL) src
ON (p.slug = src.slug)
WHEN NOT MATCHED THEN INSERT
    (seller_id, category_id, title, slug, description, item_condition,
     price, quantity, allow_meetup, allow_shipping, meetup_location,
     status, created_at, updated_at)
VALUES (
    (SELECT id FROM users      WHERE email = 'seller@glm.dev'),
    (SELECT id FROM categories WHERE slug  = 'sustainable-stationery'),
    'Eco Bamboo Notebook',
    'eco-bamboo-notebook-1',
    'Sustainable bamboo-covered A5 notebook with 100% recycled paper pages. Ideal for eco-conscious students.',
    'NEW', 12.50, 10, TRUE, FALSE, 'UTEM Main Campus',
    'ACTIVE', SYSTIMESTAMP, SYSTIMESTAMP
);

MERGE INTO products p
USING (SELECT 'stainless-water-bottle-1' AS slug FROM DUAL) src
ON (p.slug = src.slug)
WHEN NOT MATCHED THEN INSERT
    (seller_id, category_id, title, slug, description, item_condition,
     price, quantity, allow_meetup, allow_shipping, shipping_fee,
     status, created_at, updated_at)
VALUES (
    (SELECT id FROM users      WHERE email = 'seller@glm.dev'),
    (SELECT id FROM categories WHERE slug  = 'reusable-bottles-containers'),
    'Stainless Steel Water Bottle 500ml',
    'stainless-water-bottle-1',
    'BPA-free double-wall stainless steel bottle. Keeps drinks cold 24h, hot 12h. Zero plastic waste.',
    'NEW', 25.00, 5, FALSE, TRUE, 5.00,
    'ACTIVE', SYSTIMESTAMP, SYSTIMESTAMP
);
