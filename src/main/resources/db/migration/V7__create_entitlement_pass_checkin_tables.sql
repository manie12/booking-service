-- ============================================================
-- V7: Entitlements, Entitlement Usage Events, Access Passes,
--     Check-Ins
-- ============================================================
SET search_path TO booking;

CREATE TABLE entitlements (
                              id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              tenant_id           UUID NOT NULL,
                              entitlement_number  VARCHAR(100) NOT NULL,
                              order_id            UUID REFERENCES orders (id),
                              order_item_id       UUID REFERENCES order_items (id),
                              booking_id          UUID REFERENCES bookings (id),
                              booking_item_id     UUID REFERENCES booking_items (id),
                              guest_profile_id    UUID REFERENCES guest_profiles (id),
                              entitlement_type    VARCHAR(100) NOT NULL,
                              status              entitlement_status NOT NULL DEFAULT 'ISSUED',
                              issued_at           TIMESTAMPTZ,
                              valid_from          TIMESTAMPTZ,
                              valid_to            TIMESTAMPTZ,
                              usage_limit         INTEGER NOT NULL DEFAULT 1,
                              usage_count         INTEGER NOT NULL DEFAULT 0,
                              notes               TEXT,
                              created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
                              updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
                              CONSTRAINT uq_entitlements_tenant_number UNIQUE (tenant_id, entitlement_number),
                              CONSTRAINT chk_entitlements_usage CHECK (usage_count >= 0 AND usage_limit >= 0)
);

CREATE INDEX idx_entitlements_order_id ON entitlements (order_id);
CREATE INDEX idx_entitlements_order_item_id ON entitlements (order_item_id);
CREATE INDEX idx_entitlements_booking_id ON entitlements (booking_id);
CREATE INDEX idx_entitlements_booking_item_id ON entitlements (booking_item_id);
CREATE INDEX idx_entitlements_guest_profile_id ON entitlements (guest_profile_id);
CREATE INDEX idx_entitlements_tenant_id ON entitlements (tenant_id);
CREATE INDEX idx_entitlements_status ON entitlements (status);

CREATE TABLE entitlement_usage_events (
                                          id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                          entitlement_id    UUID NOT NULL REFERENCES entitlements (id) ON DELETE CASCADE,
                                          event_type        usage_event_type NOT NULL,
                                          usage_delta       INTEGER NOT NULL DEFAULT 0,
                                          event_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
                                          location_code     VARCHAR(100),
                                          reference_code    VARCHAR(200),
                                          payload_json      TEXT,
                                          created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_entitlement_usage_events_entitlement_id ON entitlement_usage_events (entitlement_id);

CREATE TABLE access_passes (
                               id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               entitlement_id   UUID NOT NULL REFERENCES entitlements (id) ON DELETE CASCADE,
                               pass_number      VARCHAR(100) NOT NULL,
                               pass_type        VARCHAR(100),
                               barcode_value    VARCHAR(300),
                               qr_token         VARCHAR(500),
                               external_token   VARCHAR(500),
                               status           pass_status NOT NULL DEFAULT 'ISSUED',
                               issued_at        TIMESTAMPTZ,
                               expires_at       TIMESTAMPTZ,
                               created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
                               CONSTRAINT uq_access_passes_pass_number UNIQUE (pass_number)
);

CREATE INDEX idx_access_passes_entitlement_id ON access_passes (entitlement_id);

CREATE TABLE check_ins (
                           id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           tenant_id        UUID NOT NULL,
                           entitlement_id   UUID NOT NULL REFERENCES entitlements (id),
                           booking_id       UUID REFERENCES bookings (id),
                           booking_item_id  UUID REFERENCES booking_items (id),
                           guest_profile_id UUID REFERENCES guest_profiles (id),
                           check_in_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                           location_code    VARCHAR(100) NOT NULL,
                           device_code      VARCHAR(100) NOT NULL,
                           actor_type       actor_type NOT NULL DEFAULT 'SYSTEM',
                           actor_id         VARCHAR(200),
                           result_status    check_in_result_status NOT NULL,
                           notes            TEXT,
                           created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_check_ins_entitlement_id ON check_ins (entitlement_id);
CREATE INDEX idx_check_ins_booking_id ON check_ins (booking_id);
CREATE INDEX idx_check_ins_booking_item_id ON check_ins (booking_item_id);
CREATE INDEX idx_check_ins_tenant_id ON check_ins (tenant_id);