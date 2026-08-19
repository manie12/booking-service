-- ============================================================
-- V5: Orders, Order Items, Order Item Guests,
--     Order Price Components, Order Status History
-- ============================================================
SET search_path TO booking;

CREATE TABLE orders (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID NOT NULL,
    order_number      VARCHAR(100) NOT NULL,
    customer_id       UUID REFERENCES customers (id),
    cart_id           UUID REFERENCES carts (id),
    channel_id        UUID,
    country_code      VARCHAR(10),
    currency_code     VARCHAR(10) NOT NULL,
    status            order_status NOT NULL DEFAULT 'DRAFT',
    payment_status    payment_status NOT NULL DEFAULT 'INITIATED',
    subtotal_amount   NUMERIC(14, 2) NOT NULL DEFAULT 0,
    discount_amount   NUMERIC(14, 2) NOT NULL DEFAULT 0,
    tax_amount        NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_amount      NUMERIC(14, 2) NOT NULL DEFAULT 0,
    paid_amount       NUMERIC(14, 2) NOT NULL DEFAULT 0,
    balance_amount    NUMERIC(14, 2) NOT NULL DEFAULT 0,
    idempotency_key   VARCHAR(200),
    placed_at         TIMESTAMPTZ,
    notes             TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_orders_tenant_number UNIQUE (tenant_id, order_number),
    CONSTRAINT uq_orders_idempotency_key UNIQUE (idempotency_key)
);

CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_orders_cart_id ON orders (cart_id);
CREATE INDEX idx_orders_tenant_id ON orders (tenant_id);
CREATE INDEX idx_orders_status ON orders (status);

CREATE TABLE order_items (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id               UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    line_number            INTEGER NOT NULL,
    product_id             UUID NOT NULL,
    product_variant_id     UUID,
    offer_id               UUID,
    price_rule_id          UUID,
    product_code_snapshot  VARCHAR(100),
    product_name_snapshot  VARCHAR(200),
    variant_code_snapshot  VARCHAR(100),
    variant_name_snapshot  VARCHAR(200),
    offer_code_snapshot    VARCHAR(100),
    offer_name_snapshot    VARCHAR(200),
    quantity               INTEGER NOT NULL DEFAULT 1,
    unit_price             NUMERIC(14, 2) NOT NULL,
    subtotal_amount        NUMERIC(14, 2) NOT NULL DEFAULT 0,
    discount_amount        NUMERIC(14, 2) NOT NULL DEFAULT 0,
    tax_amount             NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_amount           NUMERIC(14, 2) NOT NULL DEFAULT 0,
    currency_code          VARCHAR(10) NOT NULL,
    fulfillment_type       fulfillment_type NOT NULL DEFAULT 'NO_FULFILLMENT',
    service_date           DATE,
    schedule_instance_id   UUID REFERENCES schedule_instances (id),
    status                 order_status NOT NULL DEFAULT 'DRAFT',
    notes                  TEXT,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_order_items_order_line UNIQUE (order_id, line_number),
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_schedule_instance_id ON order_items (schedule_instance_id);

CREATE TABLE order_item_guests (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id        UUID NOT NULL REFERENCES order_items (id) ON DELETE CASCADE,
    guest_profile_id     UUID REFERENCES guest_profiles (id),
    guest_first_name     VARCHAR(100),
    guest_last_name      VARCHAR(100),
    guest_date_of_birth  DATE,
    guest_type           guest_type NOT NULL DEFAULT 'ADULT',
    ticket_required      BOOLEAN NOT NULL DEFAULT FALSE,
    waiver_required      BOOLEAN NOT NULL DEFAULT FALSE,
    notes                TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_order_item_guests_order_item_id ON order_item_guests (order_item_id);
CREATE INDEX idx_order_item_guests_guest_profile_id ON order_item_guests (guest_profile_id);

CREATE TABLE order_price_components (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id     UUID NOT NULL REFERENCES order_items (id) ON DELETE CASCADE,
    component_type    price_component_type NOT NULL,
    component_name    VARCHAR(200),
    source_reference  VARCHAR(200),
    amount            NUMERIC(14, 2) NOT NULL,
    currency_code     VARCHAR(10) NOT NULL,
    sort_order        INTEGER NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_order_price_components_order_item_id ON order_price_components (order_item_id);

CREATE TABLE order_status_history (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    old_status   order_status,
    new_status   order_status NOT NULL,
    actor_type   actor_type NOT NULL DEFAULT 'SYSTEM',
    actor_id     VARCHAR(200),
    reason_code  VARCHAR(100),
    notes        TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_order_status_history_order_id ON order_status_history (order_id);
