-- ============================================================
-- V6: Bookings, Booking Items, Booking Guests,
--     Booking Status History
-- ============================================================
SET search_path TO booking;

CREATE TABLE bookings (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             UUID NOT NULL,
    booking_number        VARCHAR(100) NOT NULL,
    order_id              UUID REFERENCES orders (id),
    customer_id           UUID REFERENCES customers (id),
    channel_id            UUID,
    status                booking_status NOT NULL DEFAULT 'PENDING',
    booking_date          DATE NOT NULL,
    currency_code         VARCHAR(10) NOT NULL,
    total_booked_quantity INTEGER NOT NULL DEFAULT 0,
    notes                 TEXT,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_bookings_tenant_number UNIQUE (tenant_id, booking_number)
);

CREATE INDEX idx_bookings_order_id ON bookings (order_id);
CREATE INDEX idx_bookings_customer_id ON bookings (customer_id);
CREATE INDEX idx_bookings_tenant_id ON bookings (tenant_id);
CREATE INDEX idx_bookings_status ON bookings (status);

CREATE TABLE booking_items (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id            UUID NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
    order_item_id         UUID REFERENCES order_items (id),
    schedule_instance_id  UUID REFERENCES schedule_instances (id),
    capacity_pool_id      UUID REFERENCES capacity_pools (id),
    product_id            UUID NOT NULL,
    product_variant_id    UUID,
    offer_id              UUID,
    quantity              INTEGER NOT NULL DEFAULT 1,
    unit_count            INTEGER NOT NULL DEFAULT 1,
    start_at              TIMESTAMPTZ,
    end_at                TIMESTAMPTZ,
    status                booking_status NOT NULL DEFAULT 'PENDING',
    notes                 TEXT,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_booking_items_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_booking_items_booking_id ON booking_items (booking_id);
CREATE INDEX idx_booking_items_order_item_id ON booking_items (order_item_id);
CREATE INDEX idx_booking_items_schedule_instance_id ON booking_items (schedule_instance_id);
CREATE INDEX idx_booking_items_capacity_pool_id ON booking_items (capacity_pool_id);

CREATE TABLE booking_guests (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_item_id       UUID NOT NULL REFERENCES booking_items (id) ON DELETE CASCADE,
    order_item_guest_id   UUID REFERENCES order_item_guests (id),
    guest_profile_id      UUID REFERENCES guest_profiles (id),
    guest_first_name      VARCHAR(100),
    guest_last_name       VARCHAR(100),
    guest_date_of_birth   DATE,
    guest_type            guest_type NOT NULL DEFAULT 'ADULT',
    status                booking_status NOT NULL DEFAULT 'PENDING',
    notes                 TEXT,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_booking_guests_booking_item_id ON booking_guests (booking_item_id);
CREATE INDEX idx_booking_guests_guest_profile_id ON booking_guests (guest_profile_id);
CREATE INDEX idx_booking_guests_order_item_guest_id ON booking_guests (order_item_guest_id);

CREATE TABLE booking_status_history (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id        UUID NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
    booking_item_id   UUID REFERENCES booking_items (id) ON DELETE CASCADE,
    old_status        booking_status,
    new_status        booking_status NOT NULL,
    actor_type        actor_type NOT NULL DEFAULT 'SYSTEM',
    actor_id          VARCHAR(200),
    reason_code       VARCHAR(100),
    notes             TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_booking_status_history_booking_id ON booking_status_history (booking_id);
CREATE INDEX idx_booking_status_history_booking_item_id ON booking_status_history (booking_item_id);
