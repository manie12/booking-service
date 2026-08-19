-- ============================================================
-- V4: Carts, Cart Items, Cart Item Guests, Cart Adjustments
-- ============================================================

SET search_path TO booking;


-- ============================================================
-- CARTS
-- ============================================================

CREATE TABLE carts (
                       id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       tenant_id        UUID NOT NULL,

                       cart_number      VARCHAR(100) NOT NULL,

                       customer_id      VARCHAR(100) NOT NULL,

                       session_id       VARCHAR(200),

                       channel_id       VARCHAR(100) NOT NULL,

                       country_code     VARCHAR(10),

                       currency_code    VARCHAR(10) NOT NULL,

                       status           cart_status NOT NULL DEFAULT 'ACTIVE',

                       expires_at       TIMESTAMPTZ,

                       subtotal_amount  NUMERIC(14, 2) NOT NULL DEFAULT 0,

                       discount_amount  NUMERIC(14, 2) NOT NULL DEFAULT 0,

                       tax_amount       NUMERIC(14, 2) NOT NULL DEFAULT 0,

                       total_amount     NUMERIC(14, 2) NOT NULL DEFAULT 0,

                       notes            TEXT,

                       created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),

                       updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),

                       CONSTRAINT uq_carts_tenant_number
                           UNIQUE (tenant_id, cart_number)
);


CREATE INDEX idx_carts_customer_id
    ON carts (customer_id);

CREATE INDEX idx_carts_tenant_id
    ON carts (tenant_id);

CREATE INDEX idx_carts_status
    ON carts (status);

CREATE INDEX idx_carts_cart_number
    ON carts (cart_number);


-- ============================================================
-- CART ITEMS
-- ============================================================

CREATE TABLE cart_items (
                            id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                            cart_id               VARCHAR(100) NOT NULL,

                            product_id            UUID NOT NULL,

                            product_variant_id    UUID,

                            offer_id              UUID,

                            price_rule_id         UUID,

                            schedule_instance_id  UUID
                                REFERENCES schedule_instances (id),

                            quantity              INTEGER NOT NULL DEFAULT 1,

                            unit_price            NUMERIC(14, 2) NOT NULL,

                            subtotal_amount       NUMERIC(14, 2) NOT NULL DEFAULT 0,

                            discount_amount       NUMERIC(14, 2) NOT NULL DEFAULT 0,

                            tax_amount            NUMERIC(14, 2) NOT NULL DEFAULT 0,

                            total_amount          NUMERIC(14, 2) NOT NULL DEFAULT 0,

                            currency_code         VARCHAR(10) NOT NULL,

                            fulfillment_type      fulfillment_type
                                NOT NULL
                                DEFAULT 'NO_FULFILLMENT',

                            service_date          DATE,

                            notes                 TEXT,

                            sort_order            INTEGER NOT NULL DEFAULT 0,

                            created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),

                            updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),

                            CONSTRAINT chk_cart_items_quantity
                                CHECK (quantity > 0),

                            CONSTRAINT chk_cart_items_unit_price
                                CHECK (unit_price >= 0),

                            CONSTRAINT chk_cart_items_subtotal
                                CHECK (subtotal_amount >= 0),

                            CONSTRAINT chk_cart_items_discount
                                CHECK (discount_amount >= 0),

                            CONSTRAINT chk_cart_items_tax
                                CHECK (tax_amount >= 0),

                            CONSTRAINT chk_cart_items_total
                                CHECK (total_amount >= 0)
);


CREATE INDEX idx_cart_items_cart_id
    ON cart_items (cart_id);

CREATE INDEX idx_cart_items_product_id
    ON cart_items (product_id);

CREATE INDEX idx_cart_items_schedule_instance_id
    ON cart_items (schedule_instance_id);


-- ============================================================
-- CART ITEM GUESTS
-- ============================================================

CREATE TABLE cart_item_guests (
                                  id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                  cart_item_id         UUID NOT NULL,

                                  guest_profile_id     UUID,

                                  guest_first_name     VARCHAR(100),

                                  guest_last_name      VARCHAR(100),

                                  guest_date_of_birth  DATE,

                                  guest_type           guest_type
                                                            NOT NULL
                                                                        DEFAULT 'ADULT',

                                  notes                TEXT,

                                  created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),

                                  CONSTRAINT fk_cart_item_guests_cart_item
                                      FOREIGN KEY (cart_item_id)
                                          REFERENCES cart_items (id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_cart_item_guests_guest_profile
                                      FOREIGN KEY (guest_profile_id)
                                          REFERENCES guest_profiles (id)
);


CREATE INDEX idx_cart_item_guests_cart_item_id
    ON cart_item_guests (cart_item_id);

CREATE INDEX idx_cart_item_guests_guest_profile_id
    ON cart_item_guests (guest_profile_id);


-- ============================================================
-- CART ADJUSTMENTS
-- ============================================================

CREATE TABLE cart_adjustments (
                                  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                  cart_id          UUID NOT NULL,

                                  cart_item_id     UUID,

                                  adjustment_type  adjustment_type NOT NULL,

                                  adjustment_name  VARCHAR(200),

                                  amount           NUMERIC(14, 2) NOT NULL,

                                  currency_code    VARCHAR(10) NOT NULL,

                                  source           VARCHAR(100),

                                  reason_code      VARCHAR(100),

                                  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),

                                  CONSTRAINT fk_cart_adjustments_cart
                                      FOREIGN KEY (cart_id)
                                          REFERENCES carts (id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_cart_adjustments_cart_item
                                      FOREIGN KEY (cart_item_id)
                                          REFERENCES cart_items (id)
                                          ON DELETE CASCADE
);


CREATE INDEX idx_cart_adjustments_cart_id
    ON cart_adjustments (cart_id);

CREATE INDEX idx_cart_adjustments_cart_item_id
    ON cart_adjustments (cart_item_id);