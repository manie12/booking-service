-- ============================================================
-- V11: Correct guest_profiles.customer_id to VARCHAR(100) NOT NULL
-- ============================================================
SET search_path TO booking;

-- Drop the FK constraint referencing customers(id) since customer_id
-- will no longer be a UUID type.
ALTER TABLE guest_profiles
    DROP CONSTRAINT IF EXISTS guest_profiles_customer_id_fkey;

-- Convert the column type from UUID to VARCHAR(100)
ALTER TABLE guest_profiles
    ALTER COLUMN customer_id TYPE VARCHAR(100) USING customer_id::VARCHAR(100);

-- Enforce NOT NULL
ALTER TABLE guest_profiles
    ALTER COLUMN customer_id SET NOT NULL;
