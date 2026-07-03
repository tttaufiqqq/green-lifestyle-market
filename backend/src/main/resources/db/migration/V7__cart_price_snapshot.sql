-- V7: price_snapshot on cart_items enables price-change warnings on cart GET
-- NULL for rows created before this migration; set to product price on every write.
ALTER TABLE cart_items ADD price_snapshot NUMBER(10,2);
