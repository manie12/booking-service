-- ============================================================
-- V13: Change cart_items.cart_id from UUID to VARCHAR(100)
-- ============================================================
SET search_path TO booking;

-- 2. Convert UUID -> VARCHAR
ALTER TABLE cart_items
ALTER COLUMN cart_id TYPE VARCHAR(100)
    USING cart_id::VARCHAR(100);

-- 3. Keep the column mandatory
ALTER TABLE cart_items
    ALTER COLUMN cart_id SET NOT NULL;