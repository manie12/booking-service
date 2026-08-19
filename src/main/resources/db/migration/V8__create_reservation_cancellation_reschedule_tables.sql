-- ============================================================
-- V8: Reservation Holds, Cancellations, Reschedules
-- ============================================================
SET search_path TO booking;

CREATE TABLE reservation_holds (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             UUID NOT NULL,
    cart_id               UUID REFERENCES carts (id),
    order_id              UUID REFERENCES orders (id),
    schedule_instance_id  UUID NOT NULL REFERENCES schedule_instances (id),
    capacity_pool_id      UUID NOT NULL REFERENCES capacity_pools (id),
    hold_reference        VARCHAR(200),
    quantity_held         INTEGER NOT NULL,
    status                hold_status NOT NULL DEFAULT 'ACTIVE',
    expires_at            TIMESTAMPTZ NOT NULL,
    consumed_at           TIMESTAMPTZ,
    released_at           TIMESTAMPTZ,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_reservation_holds_quantity CHECK (quantity_held > 0)
);

CREATE INDEX idx_reservation_holds_cart_id ON reservation_holds (cart_id);
CREATE INDEX idx_reservation_holds_order_id ON reservation_holds (order_id);
CREATE INDEX idx_reservation_holds_schedule_instance_id ON reservation_holds (schedule_instance_id);
CREATE INDEX idx_reservation_holds_capacity_pool_id ON reservation_holds (capacity_pool_id);
CREATE INDEX idx_reservation_holds_status ON reservation_holds (status);

CREATE TABLE cancellations (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                UUID NOT NULL,
    order_id                 UUID REFERENCES orders (id),
    order_item_id            UUID REFERENCES order_items (id),
    booking_id               UUID REFERENCES bookings (id),
    booking_item_id          UUID REFERENCES booking_items (id),
    customer_id              UUID REFERENCES customers (id),
    status                   cancellation_status NOT NULL DEFAULT 'REQUESTED',
    reason_code              VARCHAR(100),
    reason_text              TEXT,
    policy_reference         VARCHAR(200),
    refund_eligible           BOOLEAN NOT NULL DEFAULT FALSE,
    refund_amount_estimate   NUMERIC(14, 2),
    actor_type               actor_type NOT NULL DEFAULT 'CUSTOMER',
    actor_id                 VARCHAR(200),
    requested_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at             TIMESTAMPTZ,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_cancellations_order_id ON cancellations (order_id);
CREATE INDEX idx_cancellations_order_item_id ON cancellations (order_item_id);
CREATE INDEX idx_cancellations_booking_id ON cancellations (booking_id);
CREATE INDEX idx_cancellations_booking_item_id ON cancellations (booking_item_id);
CREATE INDEX idx_cancellations_customer_id ON cancellations (customer_id);
CREATE INDEX idx_cancellations_tenant_id ON cancellations (tenant_id);

CREATE TABLE reschedules (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                 UUID NOT NULL,
    booking_id                UUID NOT NULL REFERENCES bookings (id),
    booking_item_id           UUID REFERENCES booking_items (id),
    old_schedule_instance_id  UUID NOT NULL REFERENCES schedule_instances (id),
    new_schedule_instance_id  UUID NOT NULL REFERENCES schedule_instances (id),
    old_start_at              TIMESTAMPTZ,
    new_start_at              TIMESTAMPTZ,
    reschedule_fee_amount     NUMERIC(14, 2),
    currency_code             VARCHAR(10),
    reason_code               VARCHAR(100),
    reason_text               TEXT,
    actor_type                actor_type NOT NULL DEFAULT 'CUSTOMER',
    actor_id                  VARCHAR(200),
    status                    cancellation_status NOT NULL DEFAULT 'REQUESTED',
    requested_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at              TIMESTAMPTZ,
    created_at                TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_reschedules_booking_id ON reschedules (booking_id);
CREATE INDEX idx_reschedules_booking_item_id ON reschedules (booking_item_id);
CREATE INDEX idx_reschedules_old_schedule_instance_id ON reschedules (old_schedule_instance_id);
CREATE INDEX idx_reschedules_new_schedule_instance_id ON reschedules (new_schedule_instance_id);
